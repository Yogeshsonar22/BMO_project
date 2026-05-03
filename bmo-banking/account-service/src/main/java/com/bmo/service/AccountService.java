package com.bmo.service;

import com.bmo.dto.AccountDto;
import com.bmo.dto.ApiResponse;
import com.bmo.entity.Account;
import com.bmo.entity.User;
import com.bmo.exception.AccountBlockedException;
import com.bmo.exception.ResourceNotFoundException;
import com.bmo.repository.AccountRepository;
import com.bmo.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public AccountDto.Response createAccount(AccountDto.CreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUser(user);
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency());
        account.setBalance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO);
        account.setStatus(Account.AccountStatus.ACTIVE);

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    public AccountDto.Response getAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        return toResponse(account);
    }

    public AccountDto.Response getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        return toResponse(account);
    }

    public AccountDto.BalanceResponse getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        return new AccountDto.BalanceResponse(
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus().name()
        );
    }

    public List<AccountDto.Response> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void debit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        if (account.getStatus() == Account.AccountStatus.BLOCKED) {
            throw new AccountBlockedException("Account is blocked: " + accountNumber);
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new com.bmo.exception.InsufficientFundsException("Insufficient funds in account: " + accountNumber);
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void credit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        if (account.getStatus() == Account.AccountStatus.BLOCKED) {
            throw new AccountBlockedException("Account is blocked: " + accountNumber);
        }
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        String prefix = "BMO";
        String digits = String.format("%010d", (long)(Math.random() * 9_000_000_000L) + 1_000_000_000L);
        String number = prefix + digits;
        if (accountRepository.existsByAccountNumber(number)) {
            return generateAccountNumber();
        }
        return number;
    }

    private AccountDto.Response toResponse(Account account) {
        AccountDto.Response response = new AccountDto.Response();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(account.getAccountType().name());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus().name());
        response.setCurrency(account.getCurrency());
        response.setOwnerName(account.getUser().getFirstName() + " " + account.getUser().getLastName());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }
}
