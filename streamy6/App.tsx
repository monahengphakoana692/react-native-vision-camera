// App.tsx
import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import HomeScreen from './screens/HomeScreen';
import StreamScreen from './screens/StreamScreen';
import AnalyticScreen from './screens/AnalyticScreen';
import Connection from './Database/Connection';

const Stack = createStackNavigator();

function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName="Home"
        screenOptions={{
          headerShown: false,
        }}>
        <Stack.Screen name="Home" component={HomeScreen} />
        <Stack.Screen name="StreamScreen" component={StreamScreen} />
        <Stack.Screen name="AnalyticsConnection" component={Connection}/>
        <Stack.Screen name="AnalyticScreen" component={AnalyticScreen}/>
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default App;