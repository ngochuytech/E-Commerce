package com.example.e_commerce_techshop.services.wallet;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.Transaction;
import com.example.e_commerce_techshop.models.Wallet;
import com.example.e_commerce_techshop.models.WithdrawalRequest;
import com.example.e_commerce_techshop.models.WithdrawalRequest.WithdrawalStatus;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.repositories.TransactionRepository;
import com.example.e_commerce_techshop.repositories.WalletRepository;
import com.example.e_commerce_techshop.repositories.WithdrawalRequestRepository;
import com.example.e_commerce_techshop.services.notification.INotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService implements IWalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final StoreRepository storeRepository;
    private final INotificationService notificationService;

    @Override
    public Wallet getStoreWallet(String storeId) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));

        // Tìm hoặc tạo ví mới
        return walletRepository.findByStoreId(storeId)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .store(store)
                            .balance(BigDecimal.ZERO)
                            .totalEarned(BigDecimal.ZERO)
                            .totalWithdrawn(BigDecimal.ZERO)
                            .pendingAmount(BigDecimal.ZERO)
                            .build();
                    return walletRepository.save(newWallet);
                });
    }

    @Override
    public Page<Transaction> getTransactionHistory(String storeId, Pageable pageable) throws Exception {
        Wallet wallet = getStoreWallet(storeId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);
    }

    @Override
    @Transactional
    public WithdrawalRequest createWithdrawalRequest(String storeId, BigDecimal amount,
            String bankName, String bankAccountNumber,
            String bankAccountName, String note) throws Exception {
        // Validate store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));

        // Kiểm tra xem đã có đơn rút tiền đang chờ duyệt không
        Page<WithdrawalRequest> pendingRequests = withdrawalRequestRepository.findByStatusOrderByCreatedAtDesc(
                "PENDING",
                PageRequest.of(0, 1));

        if (!pendingRequests.isEmpty()) {
            WithdrawalRequest existingRequest = pendingRequests.getContent().get(0);
            if (existingRequest.getStore().getId().equals(storeId)) {
                throw new IllegalArgumentException(
                        String.format(
                                "Cửa hàng của bạn đã có một đơn rút tiền đang chờ duyệt (ID: %s, số tiền: %s VNĐ). Vui lòng chờ admin xử lý trước khi tạo đơn mới.",
                                existingRequest.getId(), existingRequest.getAmount()));
            }
        }

        // Validate wallet và số dư
        Wallet wallet = getStoreWallet(storeId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0");
        }

        if (amount.compareTo(wallet.getBalance()) > 0) {
            throw new IllegalArgumentException(
                    String.format("Số dư không đủ. Số dư hiện tại: %s, Số tiền muốn rút: %s",
                            wallet.getBalance(), amount));
        }

        // Tạo yêu cầu rút tiền
        WithdrawalRequest request = WithdrawalRequest.builder()
                .store(store)
                .wallet(wallet)
                .amount(amount)
                .bankName(bankName)
                .bankAccountNumber(bankAccountNumber)
                .bankAccountName(bankAccountName)
                .status(WithdrawalStatus.PENDING)
                .note(note)
                .build();

        WithdrawalRequest savedRequest = withdrawalRequestRepository.save(request);

        // Tạo notification cho admin
        try {
            notificationService.createAdminNotification(
                    "Yêu cầu rút tiền từ " + store.getName(),
                    "Store " + store.getName() + " yêu cầu rút " + amount + " VNĐ vào tài khoản " + bankAccountName,
                    "WITHDRAWAL_REQUEST",
                    savedRequest.getId());
        } catch (Exception e) {
            log.error("Error creating admin notification for withdrawal request: {}", e.getMessage());
        }

        return savedRequest;
    }

    @Override
    public Page<WithdrawalRequest> getWithdrawalRequests(String storeId, Pageable pageable) throws Exception {
        return withdrawalRequestRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable);
    }

    @Override
    public WithdrawalRequest getWithdrawalRequestDetail(String storeId, String requestId) throws Exception {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy yêu cầu rút tiền"));

        if (!request.getStore().getId().equals(storeId)) {
            throw new IllegalArgumentException("Yêu cầu rút tiền không thuộc về cửa hàng này");
        }

        return request;
    }

    @Override
    public Page<WithdrawalRequest> getAllWithdrawalRequests(String status, Pageable pageable) throws Exception {

        if (status != null && !status.trim().isEmpty()) {
            return withdrawalRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }

        return withdrawalRequestRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public WithdrawalRequest rejectWithdrawalRequest(String requestId, String adminNote) throws Exception {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy yêu cầu rút tiền"));

        if (!WithdrawalRequest.WithdrawalStatus.PENDING.equals(request.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể từ chối yêu cầu ở trạng thái PENDING");
        }

        request.setStatus(WithdrawalStatus.REJECTED);
        request.setAdminNote(adminNote);

        return withdrawalRequestRepository.save(request);
    }

    @Override
    @Transactional
    public WithdrawalRequest completeWithdrawalRequest(String requestId, String adminNote) throws Exception {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy yêu cầu rút tiền"));

        if (!WithdrawalRequest.WithdrawalStatus.PENDING.equals(request.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể hoàn thành yêu cầu ở trạng thái PENDING");
        }

        Wallet wallet = request.getWallet();

        // Kiểm tra số dư
        if (request.getAmount().compareTo(wallet.getBalance()) > 0) {
            throw new IllegalArgumentException("Số dư ví không đủ để thực hiện giao dịch");
        }

        // Lưu số dư trước giao dịch
        BigDecimal balanceBefore = wallet.getBalance();

        // Trừ tiền từ ví
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(request.getAmount()));
        walletRepository.save(wallet);

        // Tạo transaction
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(Transaction.TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .description(String.format("Rút tiền về tài khoản %s - %s",
                        request.getBankName(), request.getBankAccountNumber()))
                .status("COMPLETED")
                .build();
        transaction = transactionRepository.save(transaction);

        // Cập nhật trạng thái request
        request.setStatus(WithdrawalStatus.COMPLETED);
        request.setTransaction(transaction);
        request.setAdminNote(adminNote);

        return withdrawalRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void addOrderPaymentToWallet(String storeId, String orderId, BigDecimal amount, String description)
            throws Exception {
        // Validate amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        }

        // Lấy wallet của store (hoặc tạo mới nếu chưa có)
        Wallet wallet = getStoreWallet(storeId);

        // Lưu số dư trước giao dịch
        BigDecimal balanceBefore = wallet.getBalance();

        // Cộng tiền vào ví
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setTotalEarned(wallet.getTotalEarned().add(amount));
        walletRepository.save(wallet);

        // Tạo transaction ghi nhận
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(Transaction.TransactionType.ORDER_COMPLETED)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .description(description != null ? description : String.format("Thanh toán từ đơn hàng #%s", orderId))
                .status("COMPLETED")
                .build();
        transactionRepository.save(transaction);

        System.out.println(String.format("[WalletService] Đã cộng %s vào ví shop %s từ đơn hàng #%s",
                amount, storeId, orderId));
    }
}
