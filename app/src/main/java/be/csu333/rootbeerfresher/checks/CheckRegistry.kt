package be.csu333.rootbeerfresher.checks

import android.content.pm.PackageManager
import android.os.Build
import java.io.File

object CheckRegistry {

    // Copied from com.scottyab.rootbeer.Const — update these when bumping rootbeer version
    private val ROOT_APPS = arrayOf(
        "com.noshufou.android.su", "com.noshufou.android.su.elite",
        "eu.chainfire.supersu", "com.koushikdutta.superuser",
        "com.thirdparty.superuser", "com.yellowes.su",
        "com.topjohnwu.magisk", "com.kingroot.kinguser",
        "com.kingo.root", "com.smedialink.oneclickroot",
        "com.zhiqupk.root.global", "com.alephzain.framaroot"
    )

    private val DANGEROUS_APPS = arrayOf(
        "com.koushikdutta.rommanager", "com.koushikdutta.rommanager.license",
        "com.dimonvideo.luckypatcher", "com.chelpus.lackypatch",
        "com.ramdroid.appquarantine", "com.ramdroid.appquarantinepro",
        "com.android.vending.billing.InAppBillingService.COIN",
        "com.android.vending.billing.InAppBillingService.LUCK",
        "com.chelpus.luckypatcher", "com.blackmartalpha",
        "org.blackmart.market", "com.allinone.free",
        "com.repodroid.app", "org.creeplays.hack",
        "com.baseappfull.fwd", "com.zmapp",
        "com.dv.marketmod.installer", "org.mobilism.android",
        "com.android.wp.net.log", "com.android.camera.update",
        "cc.madkite.freedom", "com.solohsu.android.edxp.manager",
        "org.meowcat.edxposed.manager", "com.xmodgame",
        "com.cih.game_cih", "com.charles.lpoqasert",
        "catch_.me_.if_.you_.can_"
    )

    private val CLOAKING_APPS = arrayOf(
        "com.devadvance.rootcloak", "com.devadvance.rootcloakplus",
        "de.robv.android.xposed.installer", "com.saurik.substrate",
        "com.zachspong.temprootremovejb", "com.amphoras.hidemyroot",
        "com.amphoras.hidemyrootadfree", "com.formyhm.hiderootPremium",
        "com.formyhm.hideroot"
    )

    private val SU_PATHS = arrayOf(
        "/data/local/", "/data/local/bin/", "/data/local/xbin/",
        "/sbin/", "/su/bin/", "/system/bin/", "/system/bin/.ext/",
        "/system/bin/failsafe/", "/system/sd/xbin/",
        "/system/usr/we-need-root/", "/system/xbin/",
        "/system_ext/bin/", "/cache/", "/data/", "/dev/"
    )

    private val PROTECTED_PATHS = arrayOf(
        "/system", "/system/bin", "/system/sbin",
        "/system/xbin", "/vendor/bin", "/sbin", "/etc"
    )

    private fun foundPackages(pm: PackageManager, packages: Array<String>): List<String> =
        packages.filter { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }

    private fun foundBinaries(filename: String): List<String> =
        SU_PATHS.map { "$it$filename" }.filter { File(it).exists() }

    private fun writablePaths(): List<String> =
        PROTECTED_PATHS.filter { File(it).canWrite() }

    private fun execWhich(binary: String): String? = try {
        val process = Runtime.getRuntime().exec(arrayOf("which", binary))
        process.inputStream.bufferedReader().readLine()?.trim()
            ?.takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
        null
    }

    private fun readProp(key: String): String = try {
        val process = Runtime.getRuntime().exec(arrayOf("getprop", key))
        process.inputStream.bufferedReader().readLine()?.trim() ?: ""
    } catch (_: Exception) {
        ""
    }

    val ALL: List<RootCheck> = listOf(

        RootCheck(
            id = "test_keys",
            name = "detectTestKeys",
            description = "Checks whether the firmware is signed with test keys instead of release keys. Test-key builds are not official production firmware.",
            remediation = "Test-signed builds indicate a non-production or modified ROM. Root access is likely available.",
            methods = setOf(RootMethod.IS_ROOTED, RootMethod.IS_ROOTED_WITH_BUSYBOX)
        ) { rootBeer, _ ->
            val detected = rootBeer.detectTestKeys()
            CheckDetail(
                detected = detected,
                finding = if (detected) "Build.TAGS = ${Build.TAGS}" else null,
                techLog = "Build.TAGS = ${Build.TAGS}"
            )
        },

        RootCheck(
            id = "root_mgmt_apps",
            name = "detectRootManagementApps",
            description = "Scans installed packages for known root management apps such as Magisk Manager, SuperSU, KingRoot, and Framaroot.",
            remediation = "A root management app is installed. Root access has likely been granted to third-party apps.",
            methods = setOf(RootMethod.IS_ROOTED, RootMethod.IS_ROOTED_WITH_BUSYBOX)
        ) { rootBeer, context ->
            val detected = rootBeer.detectRootManagementApps()
            val found =
                if (detected) foundPackages(context.packageManager, ROOT_APPS) else emptyList()
            CheckDetail(
                detected = detected,
                finding = found.takeIf { it.isNotEmpty() }?.joinToString("\n"),
                techLog = "Checked ${ROOT_APPS.size} known packages; found ${found.size}"
            )
        },

        RootCheck(
            id = "dangerous_apps",
            name = "detectPotentiallyDangerousApps",
            description = "Scans for apps that typically require or facilitate elevated privileges, such as ROM managers, Lucky Patcher, and modded app stores.",
            remediation = "Potentially dangerous apps are present. These tools often require or enable root access.",
            methods = setOf(RootMethod.IS_ROOTED, RootMethod.IS_ROOTED_WITH_BUSYBOX)
        ) { rootBeer, context ->
            val detected = rootBeer.detectPotentiallyDangerousApps()
            val found =
                if (detected) foundPackages(context.packageManager, DANGEROUS_APPS) else emptyList()
            CheckDetail(
                detected = detected,
                finding = found.takeIf { it.isNotEmpty() }?.joinToString("\n"),
                techLog = "Checked ${DANGEROUS_APPS.size} known packages; found ${found.size}"
            )
        },

        RootCheck(
            id = "cloaking_apps",
            name = "detectRootCloakingApps",
            description = "Detects apps designed to hide root access from other apps, including RootCloak, Xposed Framework, and Substrate.",
            remediation = "Root-cloaking software is installed. The device is likely rooted and actively hiding it."
        ) { rootBeer, context ->
            // Avoids influence of other tests
            val test = arrayOf("su")
            val detected = rootBeer.detectRootCloakingApps(test)
            val found =
                if (detected) foundPackages(context.packageManager, CLOAKING_APPS) else emptyList()
            CheckDetail(
                detected = detected,
                finding = found.takeIf { it.isNotEmpty() }?.joinToString("\n"),
                techLog = "Checked ${CLOAKING_APPS.size} known packages; found ${found.size}"
            )
        },

        RootCheck(
            id = "su_binary",
            name = "checkForSuBinary",
            description = "Searches common filesystem paths for the 'su' binary, which grants superuser access on rooted devices.",
            remediation = "An su binary was found. This is a strong indicator of root access.",
            methods = setOf(RootMethod.IS_ROOTED, RootMethod.IS_ROOTED_WITH_BUSYBOX)
        ) { rootBeer, _ ->
            val detected = rootBeer.checkForSuBinary()
            val found = if (detected) foundBinaries("su") else emptyList()
            CheckDetail(
                detected = detected,
                finding = found.takeIf { it.isNotEmpty() }?.joinToString("\n"),
                techLog = "Searched ${SU_PATHS.size} paths for 'su'; found ${found.size}"
            )
        },

        RootCheck(
            id = "magisk_binary",
            name = "checkForMagiskBinary",
            description = "Searches common filesystem paths for the 'magisk' binary, the core component of Magisk root.",
            remediation = "Magisk binary detected. The device is rooted via Magisk.",
            methods = setOf(RootMethod.IS_ROOTED, RootMethod.IS_ROOTED_WITH_BUSYBOX)
        ) { rootBeer, _ ->
            val detected = rootBeer.checkForMagiskBinary()
            val found = if (detected) foundBinaries("magisk") else emptyList()
            CheckDetail(
                detected = detected,
                finding = found.takeIf { it.isNotEmpty() }?.joinToString("\n"),
                techLog = "Searched ${SU_PATHS.size} paths for 'magisk'; found ${found.size}"
            )
        },

        RootCheck(
            id = "busybox_binary",
            name = "checkForBusyBoxBinary",
            description = "Looks for the BusyBox binary, a collection of Unix utilities often installed alongside root.",
            remediation = "BusyBox is installed. This frequently accompanies root access.",
            methods = setOf(RootMethod.IS_ROOTED_WITH_BUSYBOX)
        ) { rootBeer, _ ->
            val detected = rootBeer.checkForBusyBoxBinary()
            val found = if (detected) foundBinaries("busybox") else emptyList()
            CheckDetail(
                detected = detected,
                finding = found.takeIf { it.isNotEmpty() }?.joinToString("\n"),
                techLog = "Searched ${SU_PATHS.size} paths for 'busybox'; found ${found.size}"
            )
        },

        RootCheck(
            id = "su_exists",
            name = "checkSuExists",
            description = "Runs 'which su' in a shell to verify whether the su command is accessible in the system PATH.",
            remediation = "The su command is accessible via PATH. Root shell access is available.",
            methods = setOf(RootMethod.IS_ROOTED, RootMethod.IS_ROOTED_WITH_BUSYBOX)
        ) { rootBeer, _ ->
            val detected = rootBeer.checkSuExists()
            val path = if (detected) execWhich("su") else null
            CheckDetail(
                detected = detected,
                finding = path?.let { "su found at $it" },
                techLog = if (detected) "which su → $path" else "which su → not found"
            )
        },

        RootCheck(
            id = "dangerous_props",
            name = "checkForDangerousProps",
            description = "Reads system properties for insecure values: ro.debuggable=1 (debug build) or ro.secure=0 (unsecured kernel).",
            remediation = "Insecure system properties detected. The kernel or build is configured to allow elevated access.",
            methods = setOf(RootMethod.IS_ROOTED, RootMethod.IS_ROOTED_WITH_BUSYBOX)
        ) { rootBeer, _ ->
            val detected = rootBeer.checkForDangerousProps()
            val propResults = if (detected) {
                buildString {
                    val debuggable = readProp("ro.debuggable")
                    val secure = readProp("ro.secure")
                    if (debuggable == "1") appendLine("ro.debuggable=1")
                    if (secure == "0") appendLine("ro.secure=0")
                }.trim()
            } else null
            CheckDetail(
                detected = detected,
                finding = propResults?.takeIf { it.isNotEmpty() },
                techLog = "Checked ro.debuggable and ro.secure"
            )
        },

        RootCheck(
            id = "rw_paths",
            name = "checkForRWPaths",
            description = "Checks whether protected system directories (/system, /vendor/bin, /sbin, /etc) are mounted read-write instead of read-only.",
            remediation = "Protected directories are writable. System partitions may have been modified.",
            methods = setOf(RootMethod.IS_ROOTED, RootMethod.IS_ROOTED_WITH_BUSYBOX)
        ) { rootBeer, _ ->
            val detected = rootBeer.checkForRWPaths()
            val found = if (detected) writablePaths() else emptyList()
            CheckDetail(
                detected = detected,
                finding = found.takeIf { it.isNotEmpty() }?.joinToString("\n"),
                techLog = "Checked ${PROTECTED_PATHS.size} protected paths; ${found.size} writable"
            )
        },

        RootCheck(
            id = "root_native",
            name = "checkForRootNative",
            description = "Uses native C++ code to scan filesystem paths for the su binary, bypassing Java-layer detection avoidance.",
            remediation = "Native detection found su. Root is confirmed at the native layer.",
            methods = setOf(RootMethod.IS_ROOTED, RootMethod.IS_ROOTED_WITH_BUSYBOX)
        ) { rootBeer, _ ->
            val detected = rootBeer.checkForRootNative()
            CheckDetail(
                detected = detected,
                techLog = "Native su scan: ${if (detected) "DETECTED" else "not found"}"
            )
        },

        RootCheck(
            id = "native_lib_read",
            name = "checkForNativeLibraryReadAccess",
            description = "Verifies whether the app can read native libraries that should be restricted, indicating a permissive SELinux policy. Returns false when access is restricted OR when the native library could not be loaded — a false result does not guarantee the check ran.",
            remediation = "Native library access is unrestricted. SELinux may be in permissive mode."
        ) { rootBeer, _ ->
            val detected = rootBeer.checkForNativeLibraryReadAccess()
            CheckDetail(
                detected = detected,
                techLog = "Native library read access: ${if (detected) "unrestricted (SELinux permissive?)" else "not detected (SELinux enforcing or native check unavailable)"}"
            )
        },

        RootCheck(
            id = "load_native_lib",
            name = "canLoadNativeLibrary",
            description = "Attempts to load the RootBeer native detection library. Failure means native checks (checkForRootNative, checkForNativeLibraryReadAccess) are inactive.",
            remediation = "If this fails, native detection is disabled. This is not a root indicator — it may mean the library is incompatible with this device."
        ) { rootBeer, _ ->
            val canLoad = rootBeer.canLoadNativeLibrary()
            CheckDetail(
                detected = !canLoad,
                techLog = "canLoadNativeLibrary: $canLoad"
            )
        }
    )
}
