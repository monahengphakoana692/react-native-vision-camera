import type {
  HostComponent,
  ViewProps,
} from 'react-native';
import { codegenNativeComponent } from 'react-native';

export interface NativeProps extends ViewProps {
  /** Start / stop camera + frame processing */
  enabled?: boolean;

  /** Toggle face detection overlay */
  showDetection?: boolean;
}

export default codegenNativeComponent<NativeProps>(
  'CustomStreamy6',
) as HostComponent<NativeProps>;
