import axios from "axios";
import i18n from "@/i18n";

enum ExceptionType {
  CantReplaceWithCollaborationException = "CantReplaceWithCollaborationException",
  CollaborationAlreadyExistsException = "CollaborationAlreadyExistsException"
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (error.response) {
      if (error.response.data?.exceptionType) {
        const exceptionType = error.response.data
          .exceptionType as ExceptionType;
        if (
          exceptionType === ExceptionType.CantReplaceWithCollaborationException
        ) {
          return i18n.global.t(
            "processList.cantReplaceWithCollaborationErrorMsg"
          );
        } else if (
          exceptionType === ExceptionType.CollaborationAlreadyExistsException
        ) {
          return i18n.global.t(
            "processList.collaborationAlreadyExistsErrorMsg",
            {
              name: error.response.data.name
            }
          );
        }
      }
    } else if (error.request) {
      return i18n.global.t("noResponseErrorMsg");
    }
  }
  return i18n.global.t("processList.unexpectedErrorMsg");
}
