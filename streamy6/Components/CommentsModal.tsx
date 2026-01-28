// screens/StreamScreen/components/CommentsModal.tsx
import React from 'react';
import {
  Modal,
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  TextInput,
  StyleSheet,
} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';

interface CommentsModalProps {
  visible: boolean;
  onClose: () => void;
  comments: string[];
  newComment: string;
  onNewCommentChange: (text: string) => void;
  onAddComment: () => void;
}

const CommentsModal: React.FC<CommentsModalProps> = ({
  visible,
  onClose,
  comments,
  newComment,
  onNewCommentChange,
  onAddComment,
}) => {
  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent={true}>
      <View style={styles.modalOverlay}>
        <View style={[styles.modalContent, styles.commentsModal]}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>
              Live Comments ({comments.length})
            </Text>
            <TouchableOpacity onPress={onClose}>
              <Icon name="close" size={24} color="#FFFFFF" />
            </TouchableOpacity>
          </View>

          <ScrollView style={styles.commentsList}>
            {comments.length === 0 ? (
              <View style={styles.noComments}>
                <Icon name="chat-bubble-outline" size={60} color="#8E8E93" />
                <Text style={styles.noCommentsText}>No comments yet</Text>
                <Text style={styles.noCommentsSubtext}>Comments will appear here when viewers join</Text>
              </View>
            ) : (
              comments.map((comment, index) => (
                <View key={index} style={styles.commentItem}>
                  <View style={styles.commentAvatar}>
                    <Icon name="person" size={20} color="#FFFFFF" />
                  </View>
                  <View style={styles.commentContent}>
                    <Text style={styles.commentAuthor}>Viewer {index + 1}</Text>
                    <Text style={styles.commentText}>{comment}</Text>
                    <Text style={styles.commentTime}>just now</Text>
                  </View>
                </View>
              ))
            )}
          </ScrollView>

          <View style={styles.commentInputContainer}>
            <TextInput
              style={styles.commentInput}
              placeholder="Add a comment..."
              placeholderTextColor="#8E8E93"
              value={newComment}
              onChangeText={onNewCommentChange}
              onSubmitEditing={onAddComment}
            />
            <TouchableOpacity 
              style={styles.sendCommentButton}
              onPress={onAddComment}
              disabled={!newComment.trim()}>
              <Icon 
                name="send" 
                size={24} 
                color={newComment.trim() ? "#007AFF" : "#8E8E93"} 
              />
            </TouchableOpacity>
          </View>
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
  commentsModal: {
    maxHeight: '90%',
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
  commentsList: {
    flex: 1,
    padding: 15,
  },
  noComments: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 60,
  },
  noCommentsText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
    marginTop: 15,
  },
  noCommentsSubtext: {
    color: '#8E8E93',
    fontSize: 14,
    marginTop: 5,
    textAlign: 'center',
  },
  commentItem: {
    flexDirection: 'row',
    marginBottom: 15,
  },
  commentAvatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#007AFF',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  commentContent: {
    flex: 1,
    backgroundColor: '#2C2C2E',
    padding: 12,
    borderRadius: 15,
    borderTopLeftRadius: 0,
  },
  commentAuthor: {
    color: '#007AFF',
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 4,
  },
  commentText: {
    color: '#FFFFFF',
    fontSize: 15,
    lineHeight: 20,
  },
  commentTime: {
    color: '#8E8E93',
    fontSize: 12,
    marginTop: 4,
  },
  commentInputContainer: {
    flexDirection: 'row',
    padding: 15,
    borderTopWidth: 1,
    borderTopColor: 'rgba(255, 255, 255, 0.1)',
  },
  commentInput: {
    flex: 1,
    backgroundColor: '#2C2C2E',
    color: '#FFFFFF',
    paddingHorizontal: 15,
    paddingVertical: 12,
    borderRadius: 20,
    fontSize: 16,
  },
  sendCommentButton: {
    marginLeft: 10,
    justifyContent: 'center',
    paddingHorizontal: 5,
  },
});

export default CommentsModal;