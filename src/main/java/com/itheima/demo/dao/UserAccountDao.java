package com.itheima.demo.dao;


import com.itheima.demo.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author:lq
 * @date: 2020/3/5
 * @time: 16:43
 */
public interface UserAccountDao extends JpaRepository<UserAccount,String> {

    List<UserAccount> findAllByAccount(String username);
}
