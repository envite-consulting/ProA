import { AbstractProcessShape } from "./AbstractProcessElement";
import { AbstractDataStoreShape } from "./AbstractDataStoreElement";
import { dia, shapes } from '@joint/core';

let linkIdCounter = 0;
const shapeNamespace = {
  ...shapes,
  AbstractProcessShape,
  AbstractDataStoreShape
};
export const graph = new dia.Graph({}, { cellNamespace: shapeNamespace });
export const paper = new dia.Paper({
  model: graph,
  cellViewNamespace: shapeNamespace,
  width: "100%",
  height: "100%",
  gridSize: 1,
  async: true,
  sorting: dia.Paper.sorting.APPROX,
  background: { color: "#F3F7F6" },
  interactive: {
    // label move is disabled by default
    labelMove: true
  },
  defaultLink: () => {
    const linkIdNumber = ++linkIdCounter;
    return new shapes.standard.Link({
      id: `link${linkIdNumber}`,

    });
  },
  defaultConnectionPoint: { name: "anchor" },
  defaultConnector: {
    name: "smooth",

  },
  validateMagnet: (sourceView, sourceMagnet) => {
    const sourceGroup = sourceView.findAttribute("port-group", sourceMagnet) || '';
    return ["end", "i-throw-event", "callActivity"].includes(sourceGroup);
  },
  validateConnection: (sourceView, sourceMagnet, targetView, targetMagnet) => {
    const targetGroup = targetView.findAttribute("port-group", targetMagnet);
    const target = targetView.model;
    return sourceView !== targetView && !target.isLink() && targetGroup == "start";
  },
  clickThreshold: 10,
  magnetThreshold: "onleave",
  linkPinning: false,
  snapLinks: { radius: 20 },
  snapLabels: true,
  markAvailable: true,
  highlighting: {
    connecting: {
      name: "mask",
      options: {
        layer: dia.Paper.Layers.BACK,
        attrs: {
          stroke: "#0057FF",
          "stroke-width": 3
        }
      }
    }
  }
});
