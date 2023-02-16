import { WebPlugin } from '@capacitor/core';

import type { DatecsPrinterPlugin, ConnectionStatus } from './definitions';

export class DatecsPrinterPluginWeb
  extends WebPlugin
  implements DatecsPrinterPlugin
{
  constructor() {
    super();
  }

  scanBluetoothDevice(): Promise<void> {
    throw new Error('Not available on web.');
  }

  connect(): Promise<void> {
    throw this.unavailable('Not available on web.');
  }

  print(): Promise<void> {
    throw this.unavailable('Not available on web.');
  }

  getBluetoothPairedDevices(): Promise<any> {
    throw this.unavailable('Not available on web.');
  }

  getConnectionStatus(): Promise<ConnectionStatus> {
    throw this.unavailable('Not available on web.');
  }
}
