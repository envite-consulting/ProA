<template>
  <v-card height="100%">

    <div id="graph-container" class="full-screen"></div>

  </v-card>
</template>

<style>
.full-screen {
  width: 100%;
  height: 100%;
}
</style>

<script lang="ts">
import { defineComponent } from 'vue';
import { shapes, dia, connectors, anchors, linkTools, util, layout } from '@joint/core';
//MIT License
import { DirectedGraph } from '@joint/layout-directed-graph';

import createAbstractProcessElement from "./AbstractProcessElement";
import { AbstractProcessShape } from "./AbstractProcessElement";

export default defineComponent({
  data: () => ({

  }),


  mounted: function () {

    const paperContainer = document.getElementById("graph-container");




    let linkIdCounter = 0;

    const shapeNamespace = { ...shapes, AbstractProcessShape };
    const graph = new dia.Graph({}, { cellNamespace: shapeNamespace });
    const paper = new dia.Paper({
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
        name: "curve",
        args: {
          sourceDirection: connectors.curve.TangentDirections.RIGHT,
          targetDirection: connectors.curve.TangentDirections.LEFT
        }
      },
      validateMagnet: (sourceView, sourceMagnet) => {
        const sourceGroup = sourceView.findAttribute("port-group", sourceMagnet);

        if (sourceGroup !== "end" && sourceGroup !== "ievent" && sourceGroup !== "callActivity") {

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

    paperContainer!.appendChild(paper.el);

    const s1 = createAbstractProcessElement("s1", "s1");

    const s2 = createAbstractProcessElement('Element 2 asdfasdf asdfasdf', "s2");

    const s3 = createAbstractProcessElement("s3", "s3");

    const s4 = createAbstractProcessElement("s4", "s4");

    const link = new shapes.standard.Link();
    link.set({
      source: { id: s1.id, port: "end-" + s1.id },
      target: { id: s4.id, port: "start-" + s4.id }
    })

    const link2 = new shapes.standard.Link();
    link2.set({
      source: { id: s1.id, port: "call-" + s1.id },
      target: { id: s3.id, port: "start-" + s3.id }
    })

    const link3 = new shapes.standard.Link();
    link3.set({
      source: { id: s4.id, port: "end-" + s4.id },
      target: { id: s2.id, port: "start-" + s2.id }
    })


    graph.addCells([s1, s2, s3, s4, link, link2, link3]);



    // Link Tools

    class PortTargetArrowhead extends linkTools.TargetArrowhead {
      preinitialize() {
        this.tagName = "rect";
        this.attributes = {
          width: 20,
          height: 14,
          x: 6, //move element
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

    let timer: NodeJS.Timeout;
    let lastView: dia.LinkView;

    paper.on("link:mouseenter", (linkView) => {
      clearTimeout(timer);
      clearTools();
      lastView = linkView;
      linkView.addTools(
        new dia.ToolsView({
          name: "onhover",
          tools: [
            new PortTargetArrowhead(),
            new linkTools.Remove({
              distance: -60,
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
          ]
        })
      );
    });

    paper.on('cell:pointerdown',
      function (cellView, evt, x, y) {
        console.log('cell view ' + cellView.model.id + ' was clicked');
      }
    );

    paper.on("link:mouseleave", (linkView) => {
      timer = setTimeout(() => clearTools(), 500);
    });

    function clearTools() {
      if (!lastView) return;
      lastView.removeTools();
    }

    // Events

    paper.on("link:connect", (linkView) => {
      const link = linkView.model;
      const source = link.source();
      const target = link.target();

    });

    paper.on("link:disconnect", (linkView, evt, prevElementView, prevMagnet) => {
      const link = linkView.model;
      const prevPort = prevElementView.findAttribute("port", prevMagnet);

    });

    graph.on("remove", (cell) => {
      if (!cell.isLink()) return;
      const source = cell.source();
      const target = cell.target();
      if (!target.id) {
        linkIdCounter--;
        return;
      }

    });

    var graphBBox = DirectedGraph.layout(graph, {
      nodeSep: 150,
      edgeSep: 80,
      rankDir: "TB",
      marginX: 10,
      marginY: 10,
    });

  }
})
</script>
