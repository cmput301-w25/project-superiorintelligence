package com.example.superior_intelligence;

import java.util.ArrayList;
import java.util.List;

public class SearchUserManager {
    public static List<UserHelper> searchPrefix(String prefix, List<UserHelper> allUsers, String currentUsername) {
        List<UserHelper> result = new ArrayList<>();
        for (UserHelper user : allUsers) {
            if (user.getUsername().startsWith(prefix) && !user.getUsername().equals(currentUsername)) {
                result.add(user);
            }
        }
        return result;
    }
}
