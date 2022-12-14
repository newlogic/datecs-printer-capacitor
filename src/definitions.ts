export interface DatecsPrinterPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
