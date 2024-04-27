import { dia, util } from '@joint/core';

export class AbstractProcessShape extends dia.Element {

  hiddenPorts: dia.Element.Port[] = [];

  hidePort(portId: string): void {
    this.hiddenPorts.push({
      id: portId,
      group: this.getPort(portId).group,
    });
    this.removePort(portId);
  }

  showAllPorts(): void {
    this.addPorts(this.hiddenPorts);
    this.hiddenPorts = [];
  }

  defaults() {
    return {
      ...super.defaults,
      type: "AbstractProcessShape",
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
                magnet: "passive"
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
                magnet: "passive",
                "stroke-width": 4
              }
            }
          },
          "i-throw-event": {
            position: "bottom",
            size: { width: 20, height: 20 },
            attrs: {

              portBody: {
                d:
                  "M -25 -calc(0.5 * h) a 10 10 0 1 0 0.00001 0 Z M -25 -calc(0.5 * h-3) a 7 7 0 1 0 0.00001 0 Z",
                magnet: "passive"
              }
            }
          },
          "i-catch-event": {
            position: "top",
            size: { width: 20, height: 20 },
            attrs: {

              portBody: {
                d:
                  "M -25 -calc(0.5 * h) a 10 10 0 1 0 0.00001 0 Z M -25 -calc(0.5 * h-3) a 7 7 0 1 0 0.00001 0 Z",
                magnet: "passive"
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
                magnet: "passive"
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

const createAbstractProcessElement = (label: string, id: number) => {

  let wrapLabel = util.breakText(label, {
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
          id: "i-throw-event-" + id,
          group: "i-throw-event",
        },
        {
          id: "i-catch-event-" + id,
          group: "i-catch-event",
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
