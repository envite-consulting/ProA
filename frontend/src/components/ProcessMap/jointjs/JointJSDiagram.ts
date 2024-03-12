import { AbstractProcessShape } from "./AbstractProcessElement";
import { shapes, dia, connectors } from '@joint/core';

let linkIdCounter = 0;
const shapeNamespace = { ...shapes, AbstractProcessShape };
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
        const sourceGroup = sourceView.findAttribute("port-group", sourceMagnet);

        if (sourceGroup !== "end" && sourceGroup !== "i-throw-event" && sourceGroup !== "callActivity") {

            return false;
        }

        return true;
    },
    validateConnection: (sourceView, sourceMagnet, targetView, targetMagnet) => {
        if (sourceView === targetView) {
            // Do not allow a loop link (starting and ending at the same element)/
            return false;
        }

        const targetGroup = targetView.findAttribute("port-group", targetMagnet);
        const targetPort = targetView.findAttribute("port", targetMagnet);
        const target = targetView.model;

        if (target.isLink()) {
            // We allow connecting only links with elements (not links with links).
            return false;
        }

        if (targetGroup !== "start") {
            // It's not possible to add inbound links to output ports (only outbound links are allowed).
            return false;
        }
        return true;
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