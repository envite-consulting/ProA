import { linkTools } from "@joint/core";

export class PortTargetArrowhead extends linkTools.TargetArrowhead {
  preinitialize() {
    this.tagName = "rect";
    this.attributes = {
      width: 20,
      height: 14,
      x: 6, // move element
      y: -7, // move element
      rx: 7,
      ry: 70,
      fill: "#FD0B88",
      "fill-opacity": 0.2,
      stroke: "#FD0B88",
      "stroke-width": 2,
      cursor: "move",
      class: "target-arrowhead"
    };
  }
}
