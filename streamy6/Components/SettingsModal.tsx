// screens/StreamScreen/components/SettingsModal.tsx
import React from 'react';
import {
  Modal,
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  Switch,
  StyleSheet,
} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';
import Slider from '@react-native-community/slider';

interface SettingsModalProps {
  visible: boolean;
  onClose: () => void;
  streamTitle: string;
  videoQuality: string;
  brightness: number;
  onShowTitleInput: () => void;
  onSetVideoQuality: (quality: string) => void;
  onSetBrightness: (value: number) => void;
}

const SettingsModal: React.FC<SettingsModalProps> = ({
  visible,
  onClose,
  streamTitle,
  videoQuality,
  brightness,
  onShowTitleInput,
  onSetVideoQuality,
  onSetBrightness,
}) => {
  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent={true}>
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Stream Settings</Text>
            <TouchableOpacity onPress={onClose}>
              <Icon name="close" size={24} color="#FFFFFF" />
            </TouchableOpacity>
          </View>

          <ScrollView style={styles.modalBody}>
            <View style={styles.settingItem}>
              <Text style={styles.settingLabel}>Stream Title</Text>
              <TouchableOpacity 
                style={styles.titleInputButton}
                onPress={onShowTitleInput}>
                <Text style={styles.titleText} numberOfLines={1}>
                  {streamTitle}
                </Text>
                <Icon name="edit" size={20} color="#8E8E93" />
              </TouchableOpacity>
            </View>

            <View style={styles.settingItem}>
              <Text style={styles.settingLabel}>Video Quality</Text>
              <View style={styles.qualityOptions}>
                {['360p', '480p', '720p', '1080p'].map(quality => (
                  <TouchableOpacity
                    key={quality}
                    style={[
                      styles.qualityOption,
                      videoQuality === quality && styles.qualityOptionActive,
                    ]}
                    onPress={() => onSetVideoQuality(quality)}>
                    <Text style={[
                      styles.qualityOptionText,
                      videoQuality === quality && styles.qualityOptionTextActive,
                    ]}>
                      {quality}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            </View>

            <View style={styles.settingItem}>
              <Text style={styles.settingLabel}>Brightness</Text>
              <View style={styles.sliderContainer}>
                <Icon name="brightness-low" size={24} color="#8E8E93" />
                <Slider
                  style={styles.slider}
                  minimumValue={0}
                  maximumValue={1}
                  value={brightness}
                  onValueChange={onSetBrightness}
                  minimumTrackTintColor="#007AFF"
                  maximumTrackTintColor="#8E8E93"
                  thumbTintColor="#007AFF"
                />
                <Icon name="brightness-high" size={24} color="#8E8E93" />
              </View>
            </View>

            <View style={styles.settingItem}>
              <View style={styles.switchSetting}>
                <View>
                  <Text style={styles.settingLabel}>Save Recording</Text>
                  <Text style={styles.settingDescription}>Save video after streaming ends</Text>
                </View>
                <Switch
                  trackColor={{ false: '#3A3A3C', true: '#007AFF' }}
                  thumbColor="#FFFFFF"
                  value={true}
                  onValueChange={() => {}}
                />
              </View>
            </View>

            <View style={styles.settingItem}>
              <View style={styles.switchSetting}>
                <View>
                  <Text style={styles.settingLabel}>Show Viewer Count</Text>
                  <Text style={styles.settingDescription}>Display number of viewers</Text>
                </View>
                <Switch
                  trackColor={{ false: '#3A3A3C', true: '#007AFF' }}
                  thumbColor="#FFFFFF"
                  value={true}
                  onValueChange={() => {}}
                />
              </View>
            </View>

            <View style={styles.settingItem}>
              <View style={styles.switchSetting}>
                <View>
                  <Text style={styles.settingLabel}>Allow Comments</Text>
                  <Text style={styles.settingDescription}>Let viewers comment on stream</Text>
                </View>
                <Switch
                  trackColor={{ false: '#3A3A3C', true: '#007AFF' }}
                  thumbColor="#FFFFFF"
                  value={true}
                  onValueChange={() => {}}
                />
              </View>
            </View>
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: '#1C1C1E',
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    maxHeight: '80%',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 15,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255, 255, 255, 0.1)',
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  modalBody: {
    padding: 20,
  },
  settingItem: {
    marginBottom: 25,
  },
  settingLabel: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
  },
  settingDescription: {
    color: '#8E8E93',
    fontSize: 14,
    marginTop: 2,
  },
  titleInputButton: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#2C2C2E',
    paddingHorizontal: 15,
    paddingVertical: 12,
    borderRadius: 10,
  },
  titleText: {
    color: '#FFFFFF',
    fontSize: 16,
    flex: 1,
    marginRight: 10,
  },
  qualityOptions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
  },
  qualityOption: {
    backgroundColor: '#2C2C2E',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 20,
  },
  qualityOptionActive: {
    backgroundColor: '#007AFF',
  },
  qualityOptionText: {
    color: '#FFFFFF',
    fontSize: 14,
  },
  qualityOptionTextActive: {
    fontWeight: 'bold',
  },
  sliderContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 10,
  },
  slider: {
    flex: 1,
    height: 40,
    marginHorizontal: 10,
  },
  switchSetting: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
});

export default SettingsModal;