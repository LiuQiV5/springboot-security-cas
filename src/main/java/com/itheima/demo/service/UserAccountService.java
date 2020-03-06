package com.itheima.demo.service;

import com.itheima.demo.dao.UserAccountDao;
import com.itheima.demo.entity.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @author:lq
 * @date: 2020/3/5
 * @time: 16:44
 */
@Service
@AllArgsConstructor
public class UserAccountService {

    private final UserAccountDao userAccountDao;

    public UserAccount findByUsername(String username){
        List<UserAccount> list = userAccountDao.findAllByAccount(username);
        return ObjectUtils.isEmpty(list)?null:list.get(0);
    }
}
