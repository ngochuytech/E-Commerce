package com.example.e_commerce_techshop.services.userWallet;

import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.models.UserTransaction;
import com.example.e_commerce_techshop.models.UserWallet;
import com.example.e_commerce_techshop.models.UserWithdrawalRequest;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.user.UserRepository;
import com.example.e_commerce_techshop.repositories.UserTransactionRepository;
import com.example.e_commerce_techshop.repositories.UserWalletRepository;
import com.example.e_commerce_techshop.repositories.UserWithdrawalRequestRepository;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserWalletService implements IUserWalletService {

    private final UserWalletRepository userWalletRepository;
    private final UserTransactionRepository userTransactionRepository;
    private final UserWithdrawalRequestRepository userWithdrawalRequestRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final INotificationService notificationService;

    @Override
    public UserWallet getOrCreateUserWallet(String userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));

        // Tìm hoặc tạo ví mới
        return userWalletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserWallet newWallet = UserWallet.builder()
                            .user(user)
                            .balance(BigDecimal.ZERO)
                            .totalRefunded(BigDecimal.ZERO)
                            .totalSpent(BigDecimal.ZERO)
                            .build();
                    return userWalletRepository.save(newWallet);
                });
    }

    @Override
    @Transactional
    public UserTransaction refundToWallet(String userId, String orderId, BigDecimal amount, String description) throws Exception {
        // Validate amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền hoàn phải lớn hơn 0");
        }

        // Validate order
        Order order = null;
        if (orderId != null && !orderId.trim().isEmpty()) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng"));
        }

        // Lấy ví của khách hàng (hoặc tạo mới nếu chưa có)
        UserWallet wallet = getOrCreateUserWallet(userId);

        // Lưu số dư trước giao dịch
        BigDecimal balanceBefore = wallet.getBalance();

        // Cộng tiền vào ví
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setTotalRefunded(wallet.getTotalRefunded().add(amount));
        userWalletRepository.save(wallet);

        // Tạo transaction ghi nhận
        UserTransaction transaction = UserTransaction.builder()
                .wallet(wallet)
                .order(order)
                .type(UserTransaction.TransactionType.REFUND)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .description(description != null ? description : 
                    String.format("Hoàn tiền từ đơn hàng %s", orderId))
                .build();
        transaction = userTransactionRepository.save(transaction);

        // Gửi thông báo cho khách hàng
        try {
            notificationService.createUserNotification(
                userId,
                "Hoàn tiền vào ví",
                "Bạn đã nhận được hoàn tiền " + amount + " VNĐ"
            );
        } catch (Exception e) {
            log.error("Error creating user notification for refund: {}", e.getMessage());
        }

        log.info("[UserWalletService] Đã hoàn {} vào ví user {} từ đơn hàng #{}", 
            amount, userId, orderId);

        return transaction;
    }
    
    @Override
    @Transactional
    public UserWithdrawalRequest createWithdrawalRequest(String userId, BigDecimal amount, String bankName, 
                                                         String bankAccountNumber, String bankAccountName, String note) throws Exception {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra xem đã có đơn rút tiền đang chờ duyệt không
        Page<UserWithdrawalRequest> pendingRequests = userWithdrawalRequestRepository.findByStatusOrderByCreatedAtDesc(
            UserWithdrawalRequest.WithdrawalStatus.PENDING.name(), 
            PageRequest.of(0, 1)
        );
        
        if (!pendingRequests.isEmpty()) {
            UserWithdrawalRequest existingRequest = pendingRequests.getContent().get(0);
            if (existingRequest.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException(
                    String.format("Bạn đã có một đơn rút tiền đang chờ duyệt (ID: %s, số tiền: %s VNĐ). Vui lòng chờ admin xử lý trước khi tạo đơn mới.",
                        existingRequest.getId(), existingRequest.getAmount()));
            }
        }

        // Validate wallet và số dư
        UserWallet wallet = getOrCreateUserWallet(userId);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0");
        }
        
        if (amount.compareTo(wallet.getBalance()) > 0) {
            throw new IllegalArgumentException(
                String.format("Số dư không đủ. Số dư hiện tại: %s, Số tiền muốn rút: %s", 
                    wallet.getBalance(), amount));
        }

        // Tạo yêu cầu rút tiền (chờ admin duyệt)
        UserWithdrawalRequest request = UserWithdrawalRequest.builder()
                .user(user)
                .wallet(wallet)
                .amount(amount)
                .bankName(bankName)
                .bankAccountNumber(bankAccountNumber)
                .bankAccountName(bankAccountName)
                .status(UserWithdrawalRequest.WithdrawalStatus.PENDING)
                .note(note)
                .build();
        
        UserWithdrawalRequest savedRequest = userWithdrawalRequestRepository.save(request);
        
        // Gửi thông báo cho khách hàng
        try {
            notificationService.createUserNotification(
                userId,
                "Yêu cầu rút tiền",
                "Yêu cầu rút " + amount + " VNĐ của bạn đã được gửi. Vui lòng chờ admin xác nhận."
            );

            notificationService.createAdminNotification(
                "Yêu cầu rút tiền mới",
                "Người dùng " + user.getFullName() + " đã tạo yêu cầu rút " + amount + " VNĐ.",
                Notification.NotificationType.PAYMENT.name(),
                request.getId()
            );
        } catch (Exception e) {
            log.error("Error creating user notification for withdrawal request: {}", e.getMessage());
        }

        log.info("[UserWalletService] User {} tạo yêu cầu rút {} vào tài khoản {} - {}", 
            userId, amount, bankName, bankAccountNumber);

        return savedRequest;
    }

    @Override
    public Page<UserWithdrawalRequest> getWithdrawalRequests(String userId, Pageable pageable) throws Exception {
        return userWithdrawalRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public UserWithdrawalRequest getWithdrawalRequestDetail(String userId, String requestId) throws Exception {
        UserWithdrawalRequest request = userWithdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy yêu cầu rút tiền"));

        if (!request.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Yêu cầu rút tiền không thuộc về người dùng này");
        }

        return request;
    }

    @Override
    public Page<UserWithdrawalRequest> getAllWithdrawalRequests(String status, Pageable pageable) throws Exception {
        if (status != null && !status.trim().isEmpty()) {
            return userWithdrawalRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        
        return userWithdrawalRequestRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public UserWithdrawalRequest rejectWithdrawalRequest(String requestId, String adminNote) throws Exception {
        UserWithdrawalRequest request = userWithdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy yêu cầu rút tiền"));

        if (!UserWithdrawalRequest.WithdrawalStatus.PENDING.equals(request.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể từ chối yêu cầu ở trạng thái PENDING");
        }

        request.setStatus(UserWithdrawalRequest.WithdrawalStatus.REJECTED);
        request.setAdminNote(adminNote);
        
        try {
            notificationService.createUserNotification(
                request.getUser().getId(),
                "Yêu cầu rút tiền bị từ chối",
                "Yêu cầu rút " + request.getAmount() + " VNĐ của bạn đã bị từ chối. Lý do: " + adminNote
            );
        } catch (Exception e) {
            log.error("Error creating user notification for rejection: {}", e.getMessage());
        }

        return userWithdrawalRequestRepository.save(request);
    }

    @Override
    @Transactional
    public UserWithdrawalRequest completeWithdrawalRequest(String requestId, String adminNote) throws Exception {
        UserWithdrawalRequest request = userWithdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy yêu cầu rút tiền"));

        if (!UserWithdrawalRequest.WithdrawalStatus.PENDING.equals(request.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể hoàn thành yêu cầu ở trạng thái PENDING");
        }

        UserWallet wallet = request.getWallet();
        
        if (request.getAmount().compareTo(wallet.getBalance()) > 0) {
            throw new IllegalArgumentException("Số dư ví không đủ để thực hiện giao dịch");
        }

        BigDecimal balanceBefore = wallet.getBalance();

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        wallet.setTotalSpent(wallet.getTotalSpent().add(request.getAmount()));
        userWalletRepository.save(wallet);

        UserTransaction transaction = UserTransaction.builder()
                .wallet(wallet)
                .type(UserTransaction.TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .description(String.format("Rút tiền về tài khoản %s - %s", 
                    request.getBankName(), request.getBankAccountNumber()))
                .build();
        transaction = userTransactionRepository.save(transaction);

        request.setStatus(UserWithdrawalRequest.WithdrawalStatus.COMPLETED);
        request.setTransaction(transaction);
        request.setAdminNote(adminNote);
        
        try {
            notificationService.createUserNotification(
                request.getUser().getId(),
                "Rút tiền thành công",
                "Yêu cầu rút " + request.getAmount() + " VNĐ của bạn đã được hoàn thành. Kiểm tra tài khoản ngân hàng của bạn."
            );
        } catch (Exception e) {
            log.error("Error creating user notification for completion: {}", e.getMessage());
        }

        return userWithdrawalRequestRepository.save(request);
    }

    @Override
    public UserWallet getWalletInfo(String userId) throws Exception {
        return getOrCreateUserWallet(userId);
    }

    @Override
    public Page<UserTransaction> getTransactionHistory(String userId, Pageable pageable) throws Exception {
        UserWallet wallet = getOrCreateUserWallet(userId);
        return userTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);
    }

    @Override
    public BigDecimal getWalletBalance(String userId) throws Exception {
        UserWallet wallet = getOrCreateUserWallet(userId);
        return wallet.getBalance();
    }

    @Override
    @Transactional
    public UserTransaction paymentFromWallet(String userId, String orderId, BigDecimal amount) throws Exception {
        // Validate amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }

        // Validate order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng"));

        // Validate wallet và số dư
        UserWallet wallet = getOrCreateUserWallet(userId);
        
        if (amount.compareTo(wallet.getBalance()) > 0) {
            throw new IllegalArgumentException(
                String.format("Số dư ví không đủ. Số dư hiện tại: %s, Số tiền cần thanh toán: %s", 
                    wallet.getBalance(), amount));
        }

        // Lưu số dư trước giao dịch
        BigDecimal balanceBefore = wallet.getBalance();

        // Trừ tiền từ ví
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setTotalSpent(wallet.getTotalSpent().add(amount));
        userWalletRepository.save(wallet);

        // Tạo transaction ghi nhận
        UserTransaction transaction = UserTransaction.builder()
                .wallet(wallet)
                .order(order)
                .type(UserTransaction.TransactionType.PAYMENT)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .description(String.format("Thanh toán đơn hàng #%s bằng ví", orderId))
                .build();
        transaction = userTransactionRepository.save(transaction);

        log.info("[UserWalletService] User {} đã thanh toán {} từ ví cho đơn hàng #{}", 
            userId, amount, orderId);

        return transaction;
    }

    @Override
    @Transactional
    public UserTransaction adjustWalletBalance(String userId, BigDecimal amount, String description) throws Exception {
        // Validate user
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Số tiền điều chỉnh phải khác 0");
        }

        // Lấy ví của khách hàng
        UserWallet wallet = getOrCreateUserWallet(userId);

        // Lưu số dư trước giao dịch
        BigDecimal balanceBefore = wallet.getBalance();

        // Cập nhật số dư
        wallet.setBalance(wallet.getBalance().add(amount));
        userWalletRepository.save(wallet);

        // Tạo transaction ghi nhận
        UserTransaction transaction = UserTransaction.builder()
                .wallet(wallet)
                .type(UserTransaction.TransactionType.ADJUSTMENT)
                .amount(amount.abs())
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .description(description != null ? description : 
                    String.format("Điều chỉnh số dư %s", (amount.compareTo(BigDecimal.ZERO) > 0 ? "tăng" : "giảm")))
                .build();
        transaction = userTransactionRepository.save(transaction);

        log.info("[UserWalletService] Điều chỉnh số dư user {} : {} ({})", 
            userId, amount, description);

        return transaction;
    }
}
