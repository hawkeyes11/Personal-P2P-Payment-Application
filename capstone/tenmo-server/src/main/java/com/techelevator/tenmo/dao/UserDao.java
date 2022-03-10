package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface UserDao {

    List<User> findAll();

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);

    User findBalanceByUsername(String username);

    List<User> findAllUsernamesAndUserIDs();

    BigDecimal getBalance(int userId);

    BigDecimal depositToBalance(BigDecimal transferAmount, String username);

    BigDecimal withdrawalFromBalance(BigDecimal transferAmount, String username);
}
