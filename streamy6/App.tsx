import React, { useEffect, useState } from 'react';
import { View, ActivityIndicator, Text } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';

import HomeScreen from './screens/HomeScreen';
import StreamScreen from './screens/StreamScreen';
import AnalyticScreen from './screens/AnalyticScreen';

import { openDatabase } from './Database';
import { createTablesAndTriggers } from './Database/schema';


const Stack = createStackNavigator();


const App = () => {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const init = async () => {
      await openDatabase();
      await createTablesAndTriggers();
      setReady(true);
    };
    init();
  }, []);

  if (!ready) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" color="#22C55E" />
        <Text style={{ color: '#FFF', marginTop: 12 }}>
          Initializing database…
        </Text>
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="Home" component={HomeScreen} />
        <Stack.Screen name="StreamScreen" component={StreamScreen} />

        <Stack.Screen name="AnalyticScreen" component={AnalyticScreen} />

      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default App;
