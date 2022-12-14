import { registerPlugin } from '@capacitor/core';

import type { DatecsPrinterPlugin } from './definitions';

const DatecsPrinter = registerPlugin<DatecsPrinterPlugin>('DatecsPrinter', {
  web: () => import('./web').then(m => new m.DatecsPrinterWeb()),
});

export * from './definitions';
export { DatecsPrinter };
