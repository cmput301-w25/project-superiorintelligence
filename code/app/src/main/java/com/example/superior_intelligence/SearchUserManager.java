package com.example.superior_intelligence;

import java.util.ArrayList;
import java.util.List;

public class SearchUserManager {
    public static List<HelperClass> searchPrefix(String prefix, List<HelperClass> allUsers, String currentUsername) {
        List<HelperClass> result = new ArrayList<>();
        for (HelperClass user : allUsers) {
            if (user.getUsername().startsWith(prefix) && !user.getUsername().equals(currentUsername)) {
                result.add(user);
            }
        }
        return result;
    }
}
