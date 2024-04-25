import { AbstractProcessShape } from "./AbstractProcessElement";
import { AbstractDataStoreShape } from "./AbstractDataStoreElement";
import { shapes, dia, connectors } from '@joint/core';

let linkIdCounter = 0;
const shapeNamespace = { ...shapes, AbstractProcessShape, AbstractDataStoreShape };
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

        return false;
    },
    validateConnection: (sourceView, sourceMagnet, targetView, targetMagnet) => {
        return false;
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