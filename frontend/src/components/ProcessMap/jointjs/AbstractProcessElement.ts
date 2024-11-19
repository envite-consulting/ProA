import { dia, util } from '@joint/core';

import { getPortPrefix } from "@/components/ProcessMap/ProcessMap.vue";
import { ProcessElementType} from "@/components/ProcessMap/types";

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

  hideActiveInstances() {
    this.attr('activeInstancesGroup/display', 'none');
  }

  setActiveInstances(activeInstancesNum: number) {
    this.attr('activeInstancesGroup/display', 'block');
    this.attr('activeInstancesText/text', activeInstancesNum.toString());
  }

  highlightPort(elementType: ProcessElementType) {
    const portId = getPortPrefix(elementType) + this.id;
    this.portProp(portId, 'attrs/portBody/stroke', "#0074FA");
  }

  unhighlightPort(elementType: ProcessElementType) {
    const portId = getPortPrefix(elementType) + this.id;
    this.portProp(portId, 'attrs/portBody/stroke', "black");
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
        },
        activeInstancesIcon: {
          x: "calc(w - 45)",
          y: "-10",
          width: "36",
          height: "20",
          rx: "10",
          ry: "10",
          fill: "#0074FA",
          stroke: "#0074FA",
          strokeWidth: 2
        },
        activeInstancesCircleInner: {
          cx: "calc(w - 34)",
          cy: "-0.4",
          r: "2.5",
          fill: "white",
        },
        activeInstancesCircleOuter: {
          cx: "calc(w - 34)",
          cy: "-0.4",
          r: "5",
          fill: "none",
          stroke: "white",
          strokeWidth: 1.5,
        },
        activeInstancesText: {
          x: "calc(w - 20)",
          y: "0.5",
          textAnchor: "middle",
          textVerticalAnchor: "middle",
          fontSize: 13,
          fontWeight: "bold",
          fill: "white"
        }
      },
      markup: [
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
        },
        {
          tagName: "g",
          selector: "activeInstancesGroup",
          children: [
            {
              tagName: "rect",
              selector: "activeInstancesIcon"
            },
            {
              tagName: "text",
              selector: "activeInstancesText"
            },
            {
              tagName: "circle",
              selector: "activeInstancesCircleInner"
            },
            {
              tagName: "circle",
              selector: "activeInstancesCircleOuter"
            }
          ]
        }
      ],
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
      },
      {
        tagName: "g",
        selector: "activeInstancesGroup",
        children: [
          {
            tagName: "rect",
            selector: "activeInstancesIcon"
          },
          {
            tagName: "text",
            selector: "activeInstancesText"
          },
          {
            tagName: "circle",
            selector: "activeInstancesCircleInner"
          },
          {
            tagName: "circle",
            selector: "activeInstancesCircleOuter"
          }
        ]
      }
    ];
  }
}

const createAbstractProcessElement = (label: string, id: number, bpmnProcessId: string) => {

  const wrapLabel = util.breakText(label, {
    width: 120
  });

  return new AbstractProcessShape({
    id: id,
    bpmnProcessId: bpmnProcessId,
    attrs: {
      label: {
        text: wrapLabel
      },
      activeInstancesGroup: {
        display: "none"
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
