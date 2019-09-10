package network.xyo.modbluetoothkotlin.client

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import kotlinx.coroutines.*
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.devices.XYCreator
import network.xyo.ble.firmware.XYBluetoothDeviceUpdate
import network.xyo.ble.firmware.XYOtaFile
import network.xyo.ble.firmware.XYOtaUpdate
import network.xyo.ble.gatt.peripheral.XYBluetoothError
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.scanner.XYScanResult
import network.xyo.ble.services.dialog.SpotaService
import network.xyo.ble.services.standard.BatteryService
import network.xyo.ble.services.standard.DeviceInformationService
import network.xyo.ble.services.xy4.PrimaryService
import network.xyo.modbluetoothkotlin.XyoUuids
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.experimental.and

open class XyoIosAppX(context: Context, scanResult: XYScanResult, hash: Int) :
        XyoBluetoothClient(context, scanResult, hash) {

    companion object : XYCreator() {

        fun enable(enable: Boolean) {
            if (enable) {
                xyoManufactureIdToCreator[XyoBluetoothClientDeviceType.IosAppX.raw] = this
            } else {
                xyoManufactureIdToCreator.remove(XyoBluetoothClientDeviceType.IosAppX.raw)
            }
        }

        override fun getDevicesFromScanResult(
                context: Context,
                scanResult: XYScanResult,
                globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>,
                foundDevices: HashMap<String,
                        XYBluetoothDevice>
        ) {
            val hash = scanResult.device?.address.hashCode()
            val createdDevice = XyoIosAppX(context, scanResult, hash)
            foundDevices[hash.toString()] = createdDevice
            globalDevices[hash.toString()] = createdDevice
        }
    }
}