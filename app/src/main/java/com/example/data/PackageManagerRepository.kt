package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.model.EcosystemApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PackageManagerRepository(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager
    private val launcherPackage: String = context.packageName

    suspend fun getEcosystemApps(manualWhitelist: Set<String>): List<EcosystemApp> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<EcosystemApp>()
        val installedApps = getInstalledApplicationsCompat()

        for (appInfo in installedApps) {
            val pkg = appInfo.packageName
            // Don't show launcher itself in the grid
            if (pkg == launcherPackage) continue

            val isHephaistos = pkg.startsWith(HEPHAISTOS_PACKAGE_PREFIX)
            val isManual = manualWhitelist.contains(pkg)

            if (isHephaistos || isManual) {
                val label = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    pkg
                }
                val icon = try {
                    packageManager.getApplicationIcon(appInfo)
                } catch (_: Exception) {
                    null
                }
                val versionName = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0)).versionName ?: "1.0"
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(pkg, 0).versionName ?: "1.0"
                    }
                } catch (_: Exception) {
                    "1.0"
                }

                apps.add(
                    EcosystemApp(
                        packageName = pkg,
                        label = label,
                        versionName = versionName,
                        isSystemHephaistos = isHephaistos,
                        isManualIncluded = isManual && !isHephaistos,
                        icon = icon
                    )
                )
            }
        }

        // Sort: genuine com.hephaistos apps first, then alphabetically
        apps.sortedWith(
            compareByDescending<EcosystemApp> { it.isSystemHephaistos }
                .thenBy { it.label.lowercase() }
        )
    }

    suspend fun getAllInstalledApps(manualWhitelist: Set<String>): List<EcosystemApp> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<EcosystemApp>()
        val installedApps = getInstalledApplicationsCompat()

        for (appInfo in installedApps) {
            val pkg = appInfo.packageName
            if (pkg == launcherPackage) continue

            val isHephaistos = pkg.startsWith(HEPHAISTOS_PACKAGE_PREFIX)
            val isManual = manualWhitelist.contains(pkg)
            val label = try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg
            }
            val icon = try {
                packageManager.getApplicationIcon(appInfo)
            } catch (_: Exception) {
                null
            }

            apps.add(
                EcosystemApp(
                    packageName = pkg,
                    label = label,
                    isSystemHephaistos = isHephaistos,
                    isManualIncluded = isManual,
                    icon = icon
                )
            )
        }

        apps.sortedWith(
            compareByDescending<EcosystemApp> { it.isSystemHephaistos || it.isManualIncluded }
                .thenBy { it.label.lowercase() }
        )
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun getInstalledApplicationsCompat(): List<ApplicationInfo> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        const val HEPHAISTOS_PACKAGE_PREFIX = "com.hephaistos."
    }
}
