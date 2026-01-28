// src/components/SimpleIcon.tsx
import React from 'react';
import { Text, StyleSheet } from 'react-native';

interface SimpleIconProps {
  name: string;
  color?: string;
  size?: number;
}

const SimpleIcon: React.FC<SimpleIconProps> = ({ name, color = '#FFFFFF', size = 24 }) => {
  const iconMap: Record<string, string> = {
    // Material Icons mapping to emojis
    'home': '🏠',
    'videocam': '🎥',
    'analytics': '📊',
    'link': '🔗',
    'settings': '⚙️',
    'wifi': '📶',
    'storage': '💾',
    'camera-alt': '📷',
    'info': 'ℹ️',
    'warning': '⚠️',
    'error': '❌',
    'check': '✅',
    'arrow-back': '⬅️',
    'menu': '☰',
    'close': '✕',
    'search': '🔍',
    'person': '👤',
    'email': '📧',
    'phone': '📱',
    'location': '📍',
    'calendar': '📅',
    'time': '⏰',
    'star': '⭐',
    'heart': '❤️',
    'share': '📤',
    'download': '⬇️',
    'upload': '⬆️',
    'lock': '🔒',
    'unlock': '🔓',
    'eye': '👁️',
    'eye-off': '👁️‍🗨️',
    'filter': '🔧',
    'sort': '↕️',
    'refresh': '🔄',
    'delete': '🗑️',
    'edit': '✏️',
    'add': '➕',
    'remove': '➖',
    'play': '▶️',
    'stop': '⏹️',
    'pause': '⏸️',
    'record': '⏺️',
    'mic': '🎤',
    'volume': '🔊',
    'mute': '🔇',
    'bell': '🔔',
    'notification': '📢',
  };

  return (
    <Text style={[styles.icon, { color, fontSize: size }]}>
      {iconMap[name] || '📱'}
    </Text>
  );
};

const styles = StyleSheet.create({
  icon: {
    fontFamily: 'System',
  },
});

export default SimpleIcon;