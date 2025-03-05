export enum SnackbarType {
  SUCCESS = "success",
  ERROR = "error"
}

export interface SnackbarConfig {
  color: string;
  icon: string;
}

export const SnackbarConfigs: Record<SnackbarType, SnackbarConfig> = {
  [SnackbarType.SUCCESS]: {
    color: "#43a047",
    icon: "mdi-emoticon-happy-outline"
  },
  [SnackbarType.ERROR]: {
    color: "#e53935",
    icon: "mdi-emoticon-sad-outline"
  }
};
