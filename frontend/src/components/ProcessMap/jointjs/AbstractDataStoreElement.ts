import { dia, util } from "@joint/core";

export class AbstractDataStoreShape extends dia.Element {
  defaults() {
    return {
      ...super.defaults,
      type: "AbstractDataStoreShape",
      size: { width: 90, height: 90 },
      attrs: {
        root: {
          magnet: true
        },
        body: {
          stroke: "#333333",
          fill: "#fff",
          strokeWidth: 2,
          d: "M 0 0 C 15 10, calc(w-15) 10, calc(w) 0 l 0 calc(h) c -15 10, -calc(w-15) 10, -calc(w) 0 l 0 -calc(h) C 15 -10, calc(w-15) -10, calc(w) 0 v 15 c -15 10, -calc(w-15) 10, -calc(w) 0"
        },
        label: {
          x: "calc(0.5 * w)",
          y: "30",
          textAnchor: "middle",
          textVerticalAnchor: "top",
          fontSize: 17,
          fontFamily: "sans-serif"
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

  adjustFontSizeAndWrap(label: string, maxWidth: number, maxHeight: number) {
    let fontSize = this.attr("label/fontSize");
    let wrapLabel = util.breakText(label, { width: maxWidth });
    let currentHeight = this.get("size")!.height;

    while (this.doesTextOverflow(wrapLabel) && fontSize > 10) {
      fontSize -= 1;
      this.attr("label/fontSize", fontSize);
      wrapLabel = util.breakText(label, { width: maxWidth });

      if (this.doesTextOverflow(wrapLabel)) {
        currentHeight += 10;
        this.resize(this.get("size")!.width, currentHeight);
      }
    }

    this.attr("label/text", wrapLabel);
  }

  doesTextOverflow(wrappedText: string): boolean {
    const textSize = this.measureTextSize(wrappedText);
    const shapeSize = this.get("size");
    const overflow =
      textSize.width > shapeSize!.width || textSize.height > shapeSize!.height;

    return overflow;
  }

  measureTextSize(text: string): { width: number; height: number } {
    const tempElem = document.createElement("div");
    tempElem.style.fontSize = `${this.attr("label/fontSize")}px`;
    tempElem.style.fontFamily = this.attr("label/fontFamily");
    tempElem.style.position = "absolute";
    tempElem.style.visibility = "hidden";
    tempElem.style.whiteSpace = "nowrap";
    tempElem.innerText = text;
    document.body.appendChild(tempElem);
    const size = { width: tempElem.offsetWidth, height: tempElem.offsetHeight };
    document.body.removeChild(tempElem);

    return size;
  }
}

const createAbstractDataStoreElement = (label: string, id: number) => {
  const maxWidth = 100;
  const maxHeight = 100;

  const element = new AbstractDataStoreShape({
    id: "ds-" + id,
    attrs: {
      label: {
        text: label
      }
    }
  });

  element.adjustFontSizeAndWrap(label, maxWidth, maxHeight);

  return element;
};

export default createAbstractDataStoreElement;
