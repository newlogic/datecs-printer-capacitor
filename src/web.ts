import { WebPlugin } from '@capacitor/core';

import type { DatecsPrinterPlugin } from './definitions';

export class DatecsPrinterWeb extends WebPlugin implements DatecsPrinterPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
