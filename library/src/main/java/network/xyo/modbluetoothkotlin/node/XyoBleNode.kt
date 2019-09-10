package network.xyo.modbluetoothkotlin.node

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.modbluetoothkotlin.client.XyoBluetoothClient
import network.xyo.modbluetoothkotlin.server.XyoBluetoothServer
import network.xyo.sdkcorekotlin.hashing.XyoHash
import network.xyo.sdkcorekotlin.network.XyoNetworkHandler
import network.xyo.sdkcorekotlin.network.XyoNetworkPipe
import network.xyo.sdkcorekotlin.network.XyoProcedureCatalog
import network.xyo.sdkcorekotlin.node.XyoRelayNode
import network.xyo.sdkcorekotlin.repositories.XyoBridgeQueueRepository
import network.xyo.sdkcorekotlin.repositories.XyoOriginBlockRepository
import network.xyo.sdkcorekotlin.repositories.XyoOriginChainStateRepository

open class XyoBleNode(private val procedureCatalogue: XyoProcedureCatalog,
                      blockRepository: XyoOriginBlockRepository,
                      stateRepository: XyoOriginChainStateRepository,
                      bridgeQueueRepository: XyoBridgeQueueRepository,
                      hashingProvider: XyoHash.XyoHashProvider) :
        XyoRelayNode(blockRepository, stateRepository, bridgeQueueRepository, hashingProvider) {

    private var canBoundWitness = true

    val scanCallback = object : XYSmartScan.Listener() {
        override fun entered(device: XYBluetoothDevice) {
            super.entered(device)

            if (device is XyoBluetoothClient) {
                GlobalScope.launch {
                    tryBoundWitnessWithDevice(device)
                }
            }
        }
    }

    val serverCallback = object : XyoBluetoothServer.Listener {
        override fun onPipe(pipe: XyoNetworkPipe) {
            GlobalScope.launch {
                if (canBoundWitness) {
                    canBoundWitness = false
                    val handler = XyoNetworkHandler(pipe)

                    boundWitness(handler, procedureCatalogue).await()

                    canBoundWitness = true
                    return@launch
                }

                pipe.close().await()
            }
        }
    }

    private suspend fun tryBoundWitnessWithDevice(device: XyoBluetoothClient) {
        if (canBoundWitness) {
            canBoundWitness = false

            device.connection {
                val pipe = device.createPipe().await()

                if (pipe != null) {
                    val handler = XyoNetworkHandler(pipe)

                    val bw = boundWitness(handler, procedureCatalogue).await()
                    return@connection XYBluetoothResult(bw != null)
                }

                return@connection XYBluetoothResult(false)
            }.await()


            canBoundWitness = true
        }
    }
}