import { dia } from "@joint/core";

export enum ProcessElementType {
  CALL_ACTIVITY = "CALL_ACTIVITY",
  END_EVENT = "END_EVENT",
  INTERMEDIATE_CATCH_EVENT = "INTERMEDIATE_CATCH_EVENT",
  INTERMEDIATE_THROW_EVENT = "INTERMEDIATE_THROW_EVENT",
  START_EVENT = "START_EVENT"
}

export interface Activity {
  elementId: string;
  label: string;
}

export interface Connection {
  callingElementType: ProcessElementType;
  callingProcessid: number;
  calledElementType: ProcessElementType;
  calledProcessid: number;
  id: number;
  label: string;
}

export interface MessageFlow {
  bpmnId: string;
  name: string;
  description: string;

  callingProcessId: number;
  calledProcessId: number;

  callingElementType: ProcessElementType;
  calledElementType: ProcessElementType;
}

export interface DataStore {
  id: number;
  name: string;
}

export interface DataStoreConnection {
  access: DataAccess;
  dataStoreId: number;
  id: number;
  processid: number;
}

export interface Event {
  elementId: string;
  label: string;
}

export interface FilterGraphInput {
  hideAbstractDataStores: boolean;
  hideCallActivities: boolean;
  hideConnectionLabels: boolean;
  hideIntermediateEvents: boolean;
  hideProcessesWithoutConnections: boolean;
  hideStartEndEvents: boolean;
  hideMessageFlows: boolean;
}

export interface Process {
  activities: Activity[];
  bpmnProcessId: string;
  endEvents: Event[];
  id: number;
  intermediateCatchEvents: Event[];
  intermediateThrowEvents: Event[];
  name: string;
  startEvents: Event[];
}

export interface ProcessInstance {
  bpmnProcessId: string;
  state: string;
}

export interface RouteObject {
  path: string,
  query?: {
    portId: string
  }
}

export type DataAccess = "READ" | "WRITE" | "READ_WRITE" | "NONE;";
export type HiddenLinks = { [key: string]: string };
export type HiddenPorts = { [key: string]: dia.Element.Port[] };
export type PortsInformation = { [key: string]: string[] };

export enum Role {
  ADMIN = "Admin",
  USER = "User"
}
