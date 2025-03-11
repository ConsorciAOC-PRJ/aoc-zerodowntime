package com.aoc.zerodowntime.config;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.stereotype.Component;

import java.io.PrintStream;

/**
 * Unified banner provider that handles both application startup banner and shell command banner.
 * In Spring Shell 3.x, the BannerProvider interface has been removed, so we implement both
 * Spring Boot's Banner interface and provide a shell command for displaying the banner.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShellBannerProvider implements Banner {

    /**
     * Banner content that is shared between startup and command-line display
     */
    private String getBannerContent(String springBootVersion) {
        StringBuilder banner = new StringBuilder();
        banner.append("\n");
        banner.append(" ______                ______                      _   _                 _____         _           \n");
        banner.append("|___  /               |  _  \\                    | | (_)               |_   _|       | |          \n");
        banner.append("   / / ___ _ __ ___   | | | |_____      ___ __ | |_ _ _ __ ___   ___   | | ___  ___| |_ ___ _ __ \n");
        banner.append("  / / / _ \\ '__/ _ \\  | | | / _ \\ \\ /\\ / / '_ \\| __| | '_ ` _ \\ / _ \\  | |/ _ \\/ __| __/ _ \\ '__|\n");
        banner.append(" / /_|  __/ | | (_) | | |/ / (_) \\ V  V /| | | | |_| | | | | | |  __/  | |  __/\\__ \\ ||  __/ |   \n");
        banner.append("/_____\\___|_|  \\___/  |___/ \\___/ \\_/\\_/ |_| |_|\\__|_|_| |_| |_|\\___|  \\_/\\___||___/\\__\\___|_|   \n");
        banner.append("\n");
        
        // Add version info if provided
        if (springBootVersion != null && !springBootVersion.isEmpty()) {
            banner.append("Zero Downtime Tester v1.0.0 (Spring Boot v").append(springBootVersion).append(")\n");
        } else {
            banner.append("Zero Downtime Tester v1.0.0\n");
        }
        
        banner.append("\n");
        banner.append("Available commands:\n");
        banner.append("  list                       - List available services\n");
        banner.append("  start psis [--env <environment>] [--interval-ms <interval>]\n");
        banner.append("                             - Start testing PSIS service\n");
        banner.append("  start iarxiu [--env <environment>] [--interval-ms <interval>]\n");
        banner.append("                             - Start testing iArxiu service\n");
        banner.append("  start signador [--env <environment>] [--interval-ms <interval>]\n");
        banner.append("                             - Start testing Signador service\n");
        banner.append("  start valid [--env <environment>] [--interval-ms <interval>]\n");
        banner.append("                             - Start testing VALID service\n");
        banner.append("  start enotum [--env <environment>] [--interval-ms <interval>]\n");
        banner.append("                             - Start testing eNotum service\n");
        banner.append("  start <service-number> [--env <environment>] [--interval-ms <interval>]\n");
        banner.append("                             - Start testing a service (legacy)\n");
        banner.append("  status                     - Show status of running tests\n");
        banner.append("  stop <service-name>        - Stop testing a service\n");
        banner.append("  stop-all                   - Stop all running tests\n");
        banner.append("  help                       - Display help about available commands\n");
        banner.append("  quit                       - Exit the application\n");
        banner.append("\n");
        banner.append("Type 'list' to see available services and 'help' for more details on commands.\n");
        banner.append("Use --env parameter to select environment (pre/pro). Default: pre\n");
        
        return banner.toString();
    }

    /**
     * Spring Boot Banner interface implementation.
     * This is called during application startup.
     */
    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        String version = SpringBootVersion.getVersion();
        out.print(getBannerContent(version));
    }

    /**
     * Display banner information when requested.
     * This can be called with the 'banner' command in the shell.
     */
    @ShellMethod(value = "Display application banner", key = "banner")
    public String displayBanner() {
        return getBannerContent(SpringBootVersion.getVersion());
    }
}
