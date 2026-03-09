import { Client, type IFrame, type StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { env } from '@/shared/config/env';
import { getAccessToken } from '@/shared/api';

let stompClient: Client | null = null;

export function getStompClient(): Client {
  if (!stompClient) {
    stompClient = new Client({
      webSocketFactory: () => new SockJS(env.wsUrl),
      beforeConnect: async () => {
        const token = getAccessToken();
        if (token && stompClient) {
          stompClient.connectHeaders = { Authorization: `Bearer ${token}` };
        }
      },
      reconnectDelay: 5000,
      onStompError: (frame: IFrame) => {
        console.error('STOMP error:', frame);
      },
    });
  }
  return stompClient;
}

export function connectStomp(onConnected?: () => void): void {
  const client = getStompClient();
  if (!client.active) {
    client.onConnect = () => onConnected?.();
    client.activate();
  }
}

export function disconnectStomp(): void {
  stompClient?.deactivate();
  stompClient = null;
}

export function subscribe(
  destination: string,
  callback: (body: string) => void,
): StompSubscription | null {
  const client = getStompClient();
  if (!client.connected) return null;
  return client.subscribe(destination, (msg) => callback(msg.body));
}

export function publish(destination: string, body: object): void {
  const client = getStompClient();
  if (client.connected) {
    client.publish({ destination, body: JSON.stringify(body) });
  }
}
