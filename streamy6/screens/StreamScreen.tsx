import React, { useState, useRef, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Alert,
  ActivityIndicator,
} from 'react-native';

import {
  Camera,
  useCameraDevice,
  useCameraPermission,
} from 'react-native-vision-camera';

import { useFocusEffect } from '@react-navigation/native';
import Icon from 'react-native-vector-icons/MaterialIcons';

// Components
import RightSidebar from '../Components/RightSidebar';
import SettingsModal from '../Components/SettingsModal';
import CommentsModal from '../Components/CommentsModal';
import TitleInputModal from '../Components/TitleInputModal';
import StreamStatus from '../Components/StreamStatus';

// Styles
import { styles } from '../Components/styles/StreamScreenStyles';

const StreamScreen = ({ navigation }: any) => {
  /* -------------------- STATE -------------------- */
  const [isStreaming, setIsStreaming] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [cameraType, setCameraType] = useState<'front' | 'back'>('front');
  const [streamUrl, setStreamUrl] = useState('');
  const [viewers, setViewers] = useState(0);

  const [showSettings, setShowSettings] = useState(false);
  const [showComments, setShowComments] = useState(false);
  const [showEffects, setShowEffects] = useState(false);
  const [isMuted, setIsMuted] = useState(false);
  const [flashOn, setFlashOn] = useState(false);

  const [comments, setComments] = useState<string[]>([]);
  const [newComment, setNewComment] = useState('');

  const [streamTitle, setStreamTitle] = useState('My Live Stream');
  const [showTitleInput, setShowTitleInput] = useState(false);

  /* -------------------- CAMERA -------------------- */
  const cameraRef = useRef<Camera>(null);
  const device = useCameraDevice(cameraType);
  const { hasPermission, requestPermission } = useCameraPermission();

  /* -------------------- PERMISSION -------------------- */
  const ensurePermission = useCallback(async () => {
    if (!hasPermission) {
      const granted = await requestPermission();
      if (!granted) {
        Alert.alert(
          'Permission required',
          'Camera and microphone permission is required to go live.'
        );
        return false;
      }
    }
    return true;
  }, [hasPermission]);

  /* -------------------- STREAM CONTROL -------------------- */
  const startStreaming = useCallback(async () => {
    if (isLoading) return;

    setIsLoading(true);

    const ok = await ensurePermission();
    if (!ok) {
      setIsLoading(false);
      return;
    }

    // UI-only streaming state (RTMP comes later)
    setIsStreaming(true);
    setStreamUrl('rtmp://live-api-s.facebook.com:80/rtmp/YOUR_STREAM_KEY');

    setIsLoading(false);

    Alert.alert('LIVE', 'Your stream is now live!');
  }, [ensurePermission, isLoading]);

  const stopStreaming = useCallback(() => {
    setIsStreaming(false);
    setStreamUrl('');
    setViewers(0);
    setComments([]);
  }, []);

  /* -------------------- CAMERA ACTIONS -------------------- */
  const switchCamera = useCallback(() => {
    setCameraType(prev => (prev === 'front' ? 'back' : 'front'));
  }, []);

  const toggleMute = useCallback(() => {
    setIsMuted(prev => !prev);
  }, []);

  const toggleFlash = useCallback(() => {
    setFlashOn(prev => !prev);
  }, []);

  /* -------------------- COMMENTS -------------------- */
  const addComment = useCallback(() => {
    if (!newComment.trim()) return;
    setComments(prev => [...prev.slice(-9), newComment]);
    setNewComment('');
  }, [newComment]);

  /* -------------------- FOCUS CLEANUP -------------------- */
  useFocusEffect(
    useCallback(() => {
      return () => {
        stopStreaming();
      };
    }, [stopStreaming])
  );

  /* -------------------- RENDER -------------------- */
  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#000" />

      {/* TOP BAR */}
      <View style={styles.topBar}>
        <TouchableOpacity
          onPress={() => {
            if (isStreaming) {
              Alert.alert(
                'End Stream',
                'Are you sure you want to stop streaming?',
                [
                  { text: 'Cancel', style: 'cancel' },
                  {
                    text: 'End',
                    style: 'destructive',
                    onPress: () => {
                      stopStreaming();
                      navigation.goBack();
                    },
                  },
                ]
              );
            } else {
              navigation.goBack();
            }
          }}>
          <Icon name="arrow-back" size={24} color="#fff" />
        </TouchableOpacity>

        <Text style={styles.title} numberOfLines={1}>
          {streamTitle}
        </Text>

        {isStreaming && (
          <View style={styles.liveBadge}>
            <Text style={styles.liveText}>LIVE</Text>
          </View>
        )}
      </View>

      {/* CAMERA PREVIEW */}
      <View style={styles.videoContainer}>
        {device ? (
          <Camera
            ref={cameraRef}
            style={styles.videoPreview}
            device={device}
            isActive={true}
            video={true}
            audio={!isMuted}
          />
        ) : (
          <ActivityIndicator size="large" color="#007AFF" />
        )}

        {isStreaming && <StreamStatus isStreaming={true} />}

        <RightSidebar
          isSwitchingCamera={false}
          isMuted={isMuted}
          flashOn={flashOn}
          showEffects={showEffects}
          showSettings={showSettings}
          showComments={showComments}
          commentsLength={comments.length}
          onSwitchCamera={switchCamera}
          onToggleFlash={toggleFlash}
          onToggleMute={toggleMute}
          onToggleEffects={() => setShowEffects(!showEffects)}
          onToggleSettings={() => setShowSettings(true)}
          onToggleComments={() => setShowComments(true)}
          onInvite={() => {}}
        />

        {/* BOTTOM CONTROLS */}
        <View style={styles.bottomControls}>
          {isStreaming ? (
            <TouchableOpacity
              style={styles.stopStreamButton}
              onPress={stopStreaming}>
              <Text style={styles.stopStreamText}>END STREAM</Text>
            </TouchableOpacity>
          ) : (
            <TouchableOpacity
              style={styles.startStreamButton}
              onPress={startStreaming}
              disabled={isLoading}>
              <Text style={styles.startStreamText}>
                {isLoading ? 'SETTING UP...' : 'GO LIVE'}
              </Text>
            </TouchableOpacity>
          )}
        </View>
      </View>

      {/* MODALS */}
      <SettingsModal
        visible={showSettings}
        onClose={() => setShowSettings(false)}
        streamTitle={streamTitle}
        videoQuality="720p"
        brightness={0.5}
        onShowTitleInput={() => setShowTitleInput(true)}
        onSetVideoQuality={() => {}}
        onSetBrightness={() => {}}
      />

      <CommentsModal
        visible={showComments}
        onPress={() => setShowComments(false)}
        comments={comments}
        newComment={newComment}
        onNewCommentChange={setNewComment}
        onAddComment={addComment}
      />

      <TitleInputModal
        visible={showTitleInput}
        onClose={() => setShowTitleInput(false)}
        streamTitle={streamTitle}
        onStreamTitleChange={setStreamTitle}
        onSave={() => setShowTitleInput(false)}
      />
    </SafeAreaView>
  );
};

export default StreamScreen;
