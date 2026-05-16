package be.csu333.rootbeerfresher.checks

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CheckRegistryTest {

    @Test
    fun allChecksHaveRequiredFields() {
        CheckRegistry.ALL.forEach { check ->
            assertThat(check.id).isNotEmpty()
            assertThat(check.name).isNotEmpty()
            assertThat(check.description).isNotEmpty()
            assertThat(check.remediation).isNotEmpty()
        }
    }

    @Test
    fun allCheckIdsAreUnique() {
        val ids = CheckRegistry.ALL.map { it.id }
        assertThat(ids.toSet()).hasSize(ids.size)
    }

    @Test
    fun registryHasExpectedCount() {
        assertThat(CheckRegistry.ALL).hasSize(13)
    }

    @Test
    fun toInitialState_propagatesMethodsFromRootCheck() {
        val check = RootCheck(
            id = "x", name = "X", description = "d", remediation = "r",
            methods = setOf(RootMethod.IS_ROOTED)
        ) { _, _ -> CheckDetail(detected = false) }
        val state = check.toInitialState()
        assertThat(state.methods).containsExactly(RootMethod.IS_ROOTED)
    }

    @Test
    fun toInitialState_propagatesEmptyMethodsWhenUnset() {
        val check = RootCheck("x", "X", "d", "r") { _, _ -> CheckDetail(detected = false) }
        val state = check.toInitialState()
        assertThat(state.methods).isEmpty()
    }

    @Test
    fun isRooted_mapsToExactlyExpectedCheckIds() {
        val expected = setOf(
            "test_keys", "root_mgmt_apps", "dangerous_apps", "su_binary",
            "dangerous_props", "rw_paths", "su_exists", "root_native", "magisk_binary"
        )
        val actual = CheckRegistry.ALL
            .filter { RootMethod.IS_ROOTED in it.methods }
            .map { it.id }
            .toSet()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun isRootedWithBusyBox_mapsToExactlyExpectedCheckIds() {
        val expected = setOf(
            "test_keys", "root_mgmt_apps", "dangerous_apps", "su_binary",
            "dangerous_props", "rw_paths", "su_exists", "root_native", "magisk_binary",
            "busybox_binary"
        )
        val actual = CheckRegistry.ALL
            .filter { RootMethod.IS_ROOTED_WITH_BUSYBOX in it.methods }
            .map { it.id }
            .toSet()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun informationalChecks_haveNoMethodMembership() {
        val untagged = setOf("cloaking_apps", "native_lib_read", "load_native_lib")
        CheckRegistry.ALL
            .filter { it.id in untagged }
            .forEach { assertThat(it.methods).isEmpty() }
    }
}
