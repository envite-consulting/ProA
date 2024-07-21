import { dia, linkTools } from "@joint/core";

type RemoveAction = (evt: dia.Event, linkView: dia.LinkView, toolView: dia.ToolView) => Promise<void>;

const createLinkRemoveButton = (removeAction: RemoveAction): linkTools.Remove => {
  return new linkTools.Remove({
    distance: -60,
    action: removeAction,
    markup: [
      {
        tagName: "circle",
        selector: "button",
        attributes: {
          r: 10,
          fill: "#FFD5E8",
          stroke: "#FD0B88",
          "stroke-width": 2,
          cursor: "pointer"
        }
      },
      {
        tagName: "path",
        selector: "icon",
        attributes: {
          d: "M -4 -4 4 4 M -4 4 4 -4",
          fill: "none",
          stroke: "#333",
          "stroke-width": 3,
          "pointer-events": "none"
        }
      }
    ]
  })
}

export default createLinkRemoveButton;
