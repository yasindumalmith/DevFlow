package com.example.devflowapi.terraform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Slf4j
public class TerraformRunner {

    @Value("${devflow.terraform.module-path}")
    private String modulePath;

    @Value("${devflow.terraform.working-dir}")
    private String workingDir;

    public void provision(String namespaceName, String ownerEmail) throws Exception {
        String envDir = prepareWorkingDirectory(namespaceName);
        runTerraformInit(envDir);
        runTerraformApply(envDir, namespaceName, ownerEmail);
    }

    public void destroy(String namespaceName, String ownerEmail) throws Exception {
        String envDir = workingDir + "/" + namespaceName;
        runTerraformDestroy(envDir, namespaceName, ownerEmail);
        cleanupDirectory(envDir);
    }

    private String prepareWorkingDirectory(String namespaceName) throws Exception {
        // Each environment gets its own terraform working directory
        String envDir = workingDir + "/" + namespaceName;
        Files.createDirectories(Paths.get(envDir));

        // Copy module files to working directory
        ProcessBuilder pb = new ProcessBuilder(
                "cp", "-r", modulePath + "/.", envDir
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to copy Terraform module from: " + modulePath);
        }

        return envDir;
    }

    private void runTerraformInit(String envDir) throws Exception {
        log.info("Running terraform init in {}", envDir);
        runCommand(envDir, "terraform", "init");
    }

    private void runTerraformApply(String envDir,
                                   String namespaceName,
                                   String ownerEmail) throws Exception {
        log.info("Running terraform apply for namespace: {}", namespaceName);
        runCommand(envDir,
                "terraform", "apply", "-auto-approve",
                "-var", "namespace_name=" + namespaceName,
                "-var", "owner_email=" + ownerEmail
        );
    }

    private void runTerraformDestroy(String envDir,
                                     String namespaceName,
                                     String ownerEmail) throws Exception {
        log.info("Running terraform destroy for namespace: {}", namespaceName);
        runCommand(envDir,
                "terraform", "destroy", "-auto-approve",
                "-var", "namespace_name=" + namespaceName,
                "-var", "owner_email=" + ownerEmail
        );
    }

    private void runCommand(String workDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(workDir));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Stream logs in real time
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[Terraform] {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException(
                    "Terraform command failed with exit code: " + exitCode
            );
        }
    }

    private void cleanupDirectory(String dirPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("rm", "-rf", dirPath);
        pb.start().waitFor();
    }
}