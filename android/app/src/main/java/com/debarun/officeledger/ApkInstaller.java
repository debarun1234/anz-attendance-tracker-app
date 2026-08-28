package com.debarun.officeledger;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;

/**
 * Hands a downloaded APK (already saved to disk by @capacitor/filesystem)
 * to Android's own package installer, so an in-app "Update now" tap can
 * finish without the user leaving the app to find the file themselves.
 *
 * Android still requires the user to confirm the actual install (no app
 * can silently replace itself), and — the first time this app tries it —
 * to grant "install unknown apps" for Office Ledger specifically. Both of
 * those are handled here; the JS side just calls install({path}) and
 * reacts to a resolved/rejected promise.
 */
@CapacitorPlugin(name = "ApkInstaller")
public class ApkInstaller extends Plugin {

    @PluginMethod
    public void install(PluginCall call) {
        String path = call.getString("path");
        if (path == null || path.isEmpty()) {
            call.reject("missing_path", "MISSING_PATH");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean canInstall = getContext().getPackageManager().canRequestPackageInstalls();
            if (!canInstall) {
                // First run (or the user previously declined): send them to the
                // one-time "allow this app to install unknown apps" settings
                // screen, scoped to this app, then let them retry the tap.
                Intent settingsIntent = new Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + getContext().getPackageName())
                );
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    getContext().startActivity(settingsIntent);
                } catch (Exception ignored) {
                    // Some OEM builds don't have this screen; the reject below
                    // still tells the user what's blocking them.
                }
                call.reject("install_unknown_apps", "PERMISSION_REQUIRED");
                return;
            }
        }

        try {
            String cleanPath = path.startsWith("file://") ? path.substring(7) : path;
            File file = new File(cleanPath);
            if (!file.exists()) {
                call.reject("file_not_found", "FILE_NOT_FOUND");
                return;
            }

            Uri apkUri = FileProvider.getUriForFile(
                    getContext(),
                    getContext().getPackageName() + ".fileprovider",
                    file
            );

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getContext().startActivity(installIntent);
            call.resolve();
        } catch (Exception e) {
            call.reject("install_launch_failed: " + e.getMessage(), "INSTALL_LAUNCH_FAILED", e);
        }
    }
}
