package com.jinju.festival.global.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class DotenvLoader {

    private static boolean loaded = false;

    public static synchronized void load() {
        if (loaded) {
            return;
        }

        Path envPath = findEnvFile();
        if (envPath == null || !Files.exists(envPath)) {
            log.info(".env file not found. Falling back to system environment variables and application.yml defaults.");
            loaded = true;
            return;
        }

        try {
            List<String> lines = Files.readAllLines(envPath, StandardCharsets.UTF_8);
            int count = 0;
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }

                int eqIdx = line.indexOf('=');
                String key = line.substring(0, eqIdx).trim();
                String value = line.substring(eqIdx + 1).trim();

                // 따옴표 제거 (쌍따옴표 또는 홑따옴표 감싸진 경우)
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                // 시스템 환경변수나 이미 존재하는 시스템 프로퍼티가 없을 때만 등록 (환경 변수 우선권 유지)
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                    count++;
                }
            }
            log.info("Loaded {} environment variables from {}", count, envPath.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to read .env file from {}: {}", envPath, e.getMessage());
        }

        loaded = true;
    }

    private static Path findEnvFile() {
        // 1. 현재 작업 디렉토리의 .env
        Path currentDirEnv = Paths.get(".env");
        if (Files.exists(currentDirEnv)) {
            return currentDirEnv;
        }

        // 2. 상위 디렉토리의 .env (루트/서브모듈 환경 지원)
        Path parentDirEnv = Paths.get("../.env");
        if (Files.exists(parentDirEnv)) {
            return parentDirEnv;
        }

        // 3. backend 디렉토리 하위의 .env (루트에서 실행 시)
        Path backendDirEnv = Paths.get("backend/.env");
        if (Files.exists(backendDirEnv)) {
            return backendDirEnv;
        }

        return null;
    }
}
