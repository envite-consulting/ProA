import { dia, util } from '@joint/core';

export class AbstractProcessShape extends dia.Element {

    defaults() {
        return {
            ...super.defaults,
            type: "Shape",
            size: { width: 140, height: 70 },
            attrs: {
                root: {
                    magnet: false
                },
                body: {
                    stroke: "#333333",
                    fill: "#fff",
                    strokeWidth: 2,
                    d:
                        "M 0 0 h calc(w) l 10 calc(0.5 * h) l -10 calc(0.5 * h) H 0 l 10 -calc(0.5 * h) l -10 -calc(0.5 * h) Z"
                },
                label: {
                    x: "calc(0.5 * w)",
                    y: "10",
                    textAnchor: "middle",
                    textVerticalAnchor: "top",
                    fontSize: 17,
                    fontFamily: "sans-serif"
                }
            },
            portMarkup: [
                {
                    tagName: "path",
                    selector: "portBody",
                    attributes: {
                        fill: "#FFFFFF",
                        stroke: "#333333",
                        "stroke-width": 2
                    }
                }
            ],
            ports: {
                groups: {
                    start: {
                        position: "left",
                        size: { width: 20, height: 20 },
                        attrs: {
                            portBody: {
                                d:
                                    "M 10 -calc(0.5 * h) a 10 10 0 1 0 0.00001 0 Z",
                                magnet: "active"
                            }
                        }
                    },
                    end: {
                        position: "right",
                        size: { width: 20, height: 20 },
                        attrs: {
                            portBody: {
                                d:
                                    "M 10 -calc(0.5 * h) a 10 10 0 1 0 0.00001 0 Z",
                                magnet: "active",
                                "stroke-width": 4
                            }
                        }
                    },
                    ievent: {
                        position: "bottom",
                        size: { width: 20, height: 20 },
                        attrs: {

                            portBody: {
                                d:
                                    "M -25 -calc(0.5 * h) a 10 10 0 1 0 0.00001 0 Z M -25 -calc(0.5 * h-3) a 7 7 0 1 0 0.00001 0 Z",
                                magnet: "active"
                            }
                        }
                    },
                    callActivity: {
                        position: "bottom",
                        size: { width: 20, height: 20 },
                        attrs: {
                            portBody: {
                                d:
                                    "M 35 -calc(0.5 * h) a3,3 0 0 1 3,3 v15 a3,3 0 0 1 -3,3 h-25 a3,3 0 0 1 -3,-3 v-15 a3,3 0 0 1 3,-3 z",
                                magnet: "active"
                            }
                        }
                    },
                }
            }
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

const createAbstractProcessElement = (label: string, id: string) => {

    var wrapLabel = util.breakText(label, {
        width: 120
    });

    return new AbstractProcessShape({
        id: id,
        attrs: {
            label: {
                text: wrapLabel
            }
        },
        ports: {
            items: [
                {
                    id: "start-" + id,
                    group: "start",
                },
                {
                    id: "end-" + id,
                    group: "end",
                },
                {
                    id: "ievent-" + id,
                    group: "ievent",
                },
                {
                    id: "call-" + id,
                    group: "callActivity",
                }
            ]
        }
    });
}

export default createAbstractProcessElement;