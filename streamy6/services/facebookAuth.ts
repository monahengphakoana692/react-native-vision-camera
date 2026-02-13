import { LoginManager, AccessToken } from 'react-native-fbsdk-next';

export const connectFacebook = async () => {
  try {
    const result = await LoginManager.logInWithPermissions([
      'public_profile',
      'email',
    ]);

    if (result.isCancelled) {
      return { success: false, message: 'Login cancelled' };
    }

    const data = await AccessToken.getCurrentAccessToken();

    if (!data) {
      return { success: false, message: 'Failed to get access token' };
    }

    return {
      success: true,
      token: data.accessToken.toString(),
    };

  } catch (error: unknown) {
    if (error instanceof Error) {
      return {
        success: false,
        message: error.message,
      };
    }

    return {
      success: false,
      message: 'An unknown error occurred',
    };
  }
};
