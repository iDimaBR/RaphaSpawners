package com.github.idimabr.raphaspawners.objects;

import com.google.common.collect.Lists;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SpawnerLog {

    private String title;
    private String user;
    private List<String> lore = Lists.newArrayList();
    private String date;

    public SpawnerLog(String title, String user, List<String> lore) {
        this.title = title;
        this.user = user;
        this.date = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
        this.lore = lore;
    }

    public SpawnerLog(String title, List<String> lore) {
        this.title = title;
        this.date = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
        this.lore = lore;
    }

    public SpawnerLog(String title) {
        this.title = title;
        this.date = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }
}
