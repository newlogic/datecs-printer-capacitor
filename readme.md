# datecs-printer-capacitor

Capacitor plugin for Datecs Mobile printers

## Install

```bash
npm install datecs-printer-capacitor
npx cap sync
```

## API

<docgen-index>

* [`getConnectionStatus()`](#getconnectionstatus)
* [`getBluetoothPairedDevices()`](#getbluetoothpaireddevices)
* [`addListener('bluetoothChange', ...)`](#addlistenerbluetoothchange)
* [`removeAllListeners()`](#removealllisteners)
* [`connect(...)`](#connect)
* [`print(...)`](#print)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getConnectionStatus()

```typescript
getConnectionStatus() => Promise<ConnectionStatus>
```

Returns the bluetooth datecs printer connection status.

**Returns:** <code>Promise&lt;<a href="#connectionstatus">ConnectionStatus</a>&gt;</code>

--------------------


### getBluetoothPairedDevices()

```typescript
getBluetoothPairedDevices() => Promise<any>
```

Returns the bluetooth paired devices.

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### addListener('bluetoothChange', ...)

```typescript
addListener(eventName: 'bluetoothChange', listenerFunc: (res: ConnectionStatus) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

Listens for bluetooth datecs printer connection status changes.

| Param              | Type                                                                            |
| ------------------ | ------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'bluetoothChange'</code>                                                  |
| **`listenerFunc`** | <code>(res: <a href="#connectionstatus">ConnectionStatus</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Removes all listeners

--------------------


### connect(...)

```typescript
connect(connectParam: ConnectParam) => Promise<void>
```

| Param              | Type                                                  |
| ------------------ | ----------------------------------------------------- |
| **`connectParam`** | <code><a href="#connectparam">ConnectParam</a></code> |

--------------------


### print(...)

```typescript
print(param: PrintParam) => Promise<void>
```

| Param       | Type                                              |
| ----------- | ------------------------------------------------- |
| **`param`** | <code><a href="#printparam">PrintParam</a></code> |

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


### Type Aliases


#### ConnectionStatus

<code>{ status: string; }</code>


#### ConnectParam

<code>{ address: string; }</code>


#### PrintParam

<code>{ content: string; }</code>

</docgen-api>
