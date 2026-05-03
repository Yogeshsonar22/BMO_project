package com.bmo.service;

import com.bmo.dto.AccountDto;
import com.bmo.entity.Account;
import com.bmo.entity.User;
import com.bmo.exception.InsufficientFundsException;
import com.bmo.exception.ResourceNotFoundException;
import com.bmo.repository.AccountRepository;
import com.bmo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setRole(User.Role.CUSTOMER);

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber("BMO1234567890");
        testAccount.setUser(testUser);
        testAccount.setAccountType(Account.AccountType.CHEQUING);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setStatus(Account.AccountStatus.ACTIVE);
        testAccount.setCurrency("CAD");
    }

    @Test
    void createAccount_Success() {
        AccountDto.CreateRequest request = new AccountDto.CreateRequest();
        request.setUserId(1L);
        request.setAccountType(Account.AccountType.SAVINGS);
        request.setCurrency("CAD");
        request.setInitialDeposit(BigDecimal.valueOf(500));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountDto.Response response = accountService.createAccount(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccountNumber()).isEqualTo("BMO1234567890");
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound_ThrowsException() {
        AccountDto.CreateRequest request = new AccountDto.CreateRequest();
        request.setUserId(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getBalance_Success() {
        when(accountRepository.findByAccountNumber("BMO1234567890"))
                .thenReturn(Optional.of(testAccount));

        AccountDto.BalanceResponse balance = accountService.getBalance("BMO1234567890");

        assertThat(balance.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(balance.getCurrency()).isEqualTo("CAD");
    }

    @Test
    void debit_Success() {
        when(accountRepository.findByAccountNumber("BMO1234567890"))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.debit("BMO1234567890", new BigDecimal("200.00"));

        verify(accountRepository).save(argThat(acc ->
                acc.getBalance().compareTo(new BigDecimal("800.00")) == 0));
    }

    @Test
    void debit_InsufficientFunds_ThrowsException() {
        when(accountRepository.findByAccountNumber("BMO1234567890"))
                .thenReturn(Optional.of(testAccount));

        assertThatThrownBy(() -> accountService.debit("BMO1234567890", new BigDecimal("5000.00")))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void credit_Success() {
        when(accountRepository.findByAccountNumber("BMO1234567890"))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.credit("BMO1234567890", new BigDecimal("500.00"));

        verify(accountRepository).save(argThat(acc ->
                acc.getBalance().compareTo(new BigDecimal("1500.00")) == 0));
    }

    @Test
    void getAccount_NotFound_ThrowsException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }
}
