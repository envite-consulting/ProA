import { dia, util } from '@joint/core';

export class AbstractDataStoreShape extends dia.Element {

    defaults() {
        return {
            ...super.defaults,
            type: "Shape",
            size: { width: 90, height: 90 },
            attrs: {
                root: {
                    magnet: true
                },
                body: {
                    stroke: "#333333",
                    fill: "#fff",
                    strokeWidth: 2,
                    d:
                        "M 0 0 C 15 10, calc(w-15) 10, calc(w) 0 l 0 calc(h) c -15 10, -calc(w-15) 10, -calc(w) 0 l 0 -calc(h) C 15 -10, calc(w-15) -10, calc(w) 0 v 15 c -15 10, -calc(w-15) 10, -calc(w) 0"
                },
                label: {
                    x: "calc(0.5 * w)",
                    y: "30",
                    textAnchor: "middle",
                    textVerticalAnchor: "top",
                    fontSize: 17,
                    fontFamily: "sans-serif"
                }
            },
        };
    }

    preinitialize() {
        this.markup = [
            {
                tagName: "rect",
                selector: "background"
            },
            {
                tagName: "path",
                selector: "body"
            },
            {
                tagName: "text",
                selector: "label"
            }
        ];
    }
}

const createAbstractDataStoreElement = (label: string, id: number) => {

    var wrapLabel = util.breakText(label, {
        width: 60
    });

    return new AbstractDataStoreShape({
        id: "ds-"+id,
        attrs: {
            label: {
                text: wrapLabel
            }
        }
    });
}

export default createAbstractDataStoreElement;