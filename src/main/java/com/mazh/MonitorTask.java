package com.mazh;

import cn.hutool.core.net.NetUtil;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * @author mazh
 * @date 2022/2/21
 * @description 服务状态检测定时任务
 */
@Component
public class MonitorTask {

    private static final Logger log = LoggerFactory.getLogger(MonitorTask.class);

    @Value("${timeout}")
    private int timeout;

    @Value("${proxy.ip.prefix}")
    private String ip;

    @Value("${tx.browser.login}")
    private String login;

    @Value("#{'${tos}'.split(',')}")
    private String[] tos;

    @Value("#{'${site}'.split(',')}")
    private String[] site;

    private final String title = "，系统已巡检";

    @Scheduled(cron = "${cron}")
    public void monitor() {
        log.info("start...");
        try {
            boolean connected = checkVpnConnected();
            if (!connected) {
                String info = "vpn connection fail";
                fail(info);
                log.error(info);
                return;
            }
            boolean accessed = checkTxBrowser();
            if (!accessed) {
                String info = "txBrowser accessed fail";
                fail(info);
                log.error(info);
                return;
            }
            boolean checkForeignSite = checkForeignSite(site);
            if (checkForeignSite) {
                ok(Arrays.toString(site) + "，网站访问正常");
                return;
            }
            ok(Arrays.toString(site) + "，网站访问不稳定，正在排查");
            return;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("end...");
    }

    private boolean checkVpnConnected() {
        LinkedHashSet<String> ipList = NetUtil.localIpv4s();
        for (String ips : ipList) {
            if (ips.startsWith(ip)) {
                return true;
            }
        }
        log.info("ipList:{}", ipList);
        return false;
    }

    private boolean checkTxBrowser() {
        try {
            HttpResponse response = HttpRequest.get(login).timeout(timeout).execute();
            log.info("status:{}", response.getStatus());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    private boolean checkForeignSite(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                log.info("i:{}, domain:{}", i, args[i]);
                HttpRequest.get(args[i]).timeout(timeout).execute();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    private void fail(String info) {
        MailUtil.send(tos[0], LocalDate.now() + title, info + ", 请排查", false);
        log.info("email send success");
    }

    private void ok(String info) {
        MailUtil.send(Arrays.asList(tos), LocalDate.now() + title, "巡检结果如下：\n" +
                "vpn连接正常\n" +
                "tx查证平台访问正常\n" +
                info, false);
        log.info("emails send success");
    }

}
