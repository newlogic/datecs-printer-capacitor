import type { PluginListenerHandle } from '@capacitor/core';

export type ConnectionStatus = {
  status: string;
};

export type PrintParam = {
  content: string;
};

export type ConnectParam = {
  address: string;
};

export interface DatecsPrinterPlugin {
  /**
   * Returns the bluetooth datecs printer connection status.
   */
  getConnectionStatus(): Promise<ConnectionStatus>;

  /**
   * Returns the bluetooth paired devices.
   */
  getBluetoothPairedDevices(): Promise<any>;

  /**
   * Listens for bluetooth datecs printer connection status changes.
   */
  addListener(
    eventName: 'bluetoothChange',
    listenerFunc: (res: ConnectionStatus) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Removes all listeners
   */
  removeAllListeners(): Promise<void>;

  // setAddress(addressObj: BluetoothAddress): Promise<void>;

  connect(connectParam: ConnectParam): Promise<void>;

  print(param: PrintParam): Promise<void>;
}
