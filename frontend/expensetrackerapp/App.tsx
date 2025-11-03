import React, { useEffect, useRef } from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import Home from './src/app/pages/Home';
import SignUp from './src/app/pages/SignUp';
import Login from './src/app/pages/Login';
import Profile from './src/app/pages/Profile';
import { enableScreens } from 'react-native-screens';
import { GluestackUIProvider } from '@gluestack-ui/themed';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { DeviceEventEmitter, PermissionsAndroid, Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { NativeModules } from 'react-native';

enableScreens(true);

export type RootStackParamList = {
  Login: undefined;
  SignUp: undefined;
  Home: undefined;
  Profile: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

function App(): React.JSX.Element {
  useEffect(() => {
    const SERVER_BASE_URL = Platform.OS === 'android' ? 'http://localhost:8000' : 'http://localhost:8000';

    const requestSmsPermissions = async () => {
      console.log('[SMS] Requesting permissions...');
      if (Platform.OS !== 'android') return true;
      try {
        const result = await PermissionsAndroid.requestMultiple([
          PermissionsAndroid.PERMISSIONS.RECEIVE_SMS,
          PermissionsAndroid.PERMISSIONS.READ_SMS,
        ]);
        console.log('[SMS] Permission results:', result);
        return (
          result[PermissionsAndroid.PERMISSIONS.RECEIVE_SMS] === PermissionsAndroid.RESULTS.GRANTED &&
          result[PermissionsAndroid.PERMISSIONS.READ_SMS] === PermissionsAndroid.RESULTS.GRANTED
        );
      } catch (e) {
        console.log('[SMS] Permission request error', e);
        return false;
      }
    };

    const getUserId = async (): Promise<string | null> => {
      const accessToken = await AsyncStorage.getItem('accessToken');
      if (!accessToken) return null;
      try {
        const res = await fetch(`${SERVER_BASE_URL}/auth/v1/ping`, {
          method: 'GET',
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: `Bearer ${accessToken}`,
            'X-Requested-With': 'XMLHttpRequest',
          },
        });
        const text = await res.text();
        return text?.trim() || null;
      } catch {
        return null;
      }
    };

    const subscribeToSms = async () => {
      const granted = await requestSmsPermissions();
      console.log('[SMS] Permissions granted?', granted);
      if (!granted) return;

      console.log('[SMS] NativeModules.SmsListenerModule exists?', !!NativeModules.SmsListenerModule);
      if (NativeModules.SmsListenerModule?.startListeningToSMS) {
        try { 
          console.log('[SMS] Calling startListeningToSMS');
          NativeModules.SmsListenerModule.startListeningToSMS(); 
        } catch (err) {
          console.log('[SMS] startListeningToSMS error', err);
        }
      }

      const lastMsgRef = { body: '', ts: 0 } as { body: string; ts: number };
      const sub = DeviceEventEmitter.addListener('onSMSReceived', async (event: any) => {
        console.log('[SMS] Event received:', event);
        try {
          const messageBody = event?.messageBody ?? (typeof event === 'string' ? event : '');
          if (!messageBody) return;

          const now = Date.now();
          if (lastMsgRef.body === messageBody && now - lastMsgRef.ts < 2000) {
            console.log('[SMS] Duplicate suppressed');
            return;
          }
          lastMsgRef.body = messageBody;
          lastMsgRef.ts = now;

          const userId = await getUserId();
          const accessToken = await AsyncStorage.getItem('accessToken');
          if (!userId || !accessToken) return;

          const resp = await fetch(`${SERVER_BASE_URL}/v1/ds/message`, {
            method: 'POST',
            headers: {
              Accept: 'application/json',
              'Content-Type': 'application/json',
              Authorization: `Bearer ${accessToken}`,
              'X-Requested-With': 'XMLHttpRequest',
              'x-user-id': userId,
            },
            body: JSON.stringify({ message: messageBody }),
          });
          console.log('[SMS] Posted to DS, status:', resp.status);

          // Give backend a moment to process Kafka → DB, then refresh
          setTimeout(() => {
            DeviceEventEmitter.emit('expenseRefresh');
          }, 1200);
        } catch {}
      });

      return () => {
        sub.remove();
      };
    };

    const cleanupPromise = subscribeToSms();
    return () => {
      // ensure listener removal if subscribeToSms returned a cleanup
      Promise.resolve(cleanupPromise).then((cleanup: any) => {
        if (typeof cleanup === 'function') cleanup();
      });
    };
  }, []);

  return (
    <SafeAreaProvider>
      <GluestackUIProvider>
        <NavigationContainer>
          <Stack.Navigator>
            <Stack.Screen name="Login" component={Login} />
            <Stack.Screen name="SignUp" component={SignUp} />
            <Stack.Screen name="Home" component={Home} />
            <Stack.Screen
              name="Profile"
              component={Profile}
              options={{
                headerShown: true,
                headerTitle: 'Profile',
                headerShadowVisible: false,
                headerStyle: {
                  backgroundColor: 'transparent',
                },
              }}
            />
          </Stack.Navigator>
        </NavigationContainer>
      </GluestackUIProvider>
    </SafeAreaProvider>
  );
}

export default App;
