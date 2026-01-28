// screens/StreamScreen/components/RightSidebar.tsx
import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';
import { styles } from '../Components/styles/StreamScreenStyles';

interface RightSidebarProps {
  isSwitchingCamera: boolean;
  isMuted: boolean;
  flashOn: boolean;
  showEffects: boolean;
  showSettings: boolean;
  showComments: boolean;
  commentsLength: number;
  onSwitchCamera: () => void;
  onToggleFlash: () => void;
  onToggleMute: () => void;
  onToggleEffects: () => void;
  onToggleSettings: () => void;
  onToggleComments: () => void;
  onInvite: () => void;
}

const RightSidebar: React.FC<RightSidebarProps> = ({
  isSwitchingCamera,
  isMuted,
  flashOn,
  showEffects,
  showSettings,
  showComments,
  commentsLength,
  onSwitchCamera,
  onToggleFlash,
  onToggleMute,
  onToggleEffects,
  onToggleSettings,
  onToggleComments,
  onInvite,
}) => {
  return (
    <View style={styles.rightSidebar}>
      {/* Camera switch */}
      <TouchableOpacity
        style={styles.sidebarButton}
        onPress={onSwitchCamera}
        disabled={isSwitchingCamera}>
        <Icon 
          name="flip-camera-android" 
          size={28} 
          color="#FFFFFF" 
        />
        <Text style={styles.sidebarButtonText}>
          {isSwitchingCamera ? 'Switching...' : 'Flip'}
        </Text>
      </TouchableOpacity>

      {/* Flash */}
      <TouchableOpacity
        style={styles.sidebarButton}
        onPress={onToggleFlash}>
        <Icon 
          name={flashOn ? "flash-on" : "flash-off"} 
          size={28} 
          color={flashOn ? "#FFD700" : "#FFFFFF"} 
        />
        <Text style={styles.sidebarButtonText}>
          {flashOn ? 'Flash On' : 'Flash'}
        </Text>
      </TouchableOpacity>

      {/* Mute */}
      <TouchableOpacity
        style={styles.sidebarButton}
        onPress={onToggleMute}>
        <Icon 
          name={isMuted ? "mic-off" : "mic"} 
          size={28} 
          color={isMuted ? "#FF3B30" : "#FFFFFF"} 
        />
        <Text style={styles.sidebarButtonText}>
          {isMuted ? 'Muted' : 'Mic'}
        </Text>
      </TouchableOpacity>

      {/* Effects */}
      <TouchableOpacity
        style={styles.sidebarButton}
        onPress={onToggleEffects}>
        <Icon 
          name="palette" 
          size={28} 
          color={showEffects ? "#007AFF" : "#FFFFFF"} 
        />
        <Text style={styles.sidebarButtonText}>
          Effects
        </Text>
      </TouchableOpacity>

      {/* Settings */}
      <TouchableOpacity
        style={styles.sidebarButton}
        onPress={onToggleSettings}>
        <Icon 
          name="settings" 
          size={28} 
          color={showSettings ? "#007AFF" : "#FFFFFF"} 
        />
        <Text style={styles.sidebarButtonText}>
          Settings
        </Text>
      </TouchableOpacity>

      {/* Comments */}
      <TouchableOpacity
        style={styles.sidebarButton}
        onPress={onToggleComments}>
        <View style={styles.commentBadge}>
          <Icon 
            name="chat" 
            size={28} 
            color={showComments ? "#007AFF" : "#FFFFFF"} 
          />
          {commentsLength > 0 && (
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{commentsLength}</Text>
            </View>
          )}
        </View>
        <Text style={styles.sidebarButtonText}>
          Comments
        </Text>
      </TouchableOpacity>

      {/* Invite */}
      <TouchableOpacity
        style={styles.sidebarButton}
        onPress={onInvite}>
        <Icon 
          name="person-add" 
          size={28} 
          color="#FFFFFF" 
        />
        <Text style={styles.sidebarButtonText}>
          Invite
        </Text>
      </TouchableOpacity>
    </View>
  );
};

export default RightSidebar;