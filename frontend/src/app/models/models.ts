export type Role = 'EMPLOYEE' | 'ADMIN';
export type Category = 'IT' | 'BUG' | 'HR';
export type Priority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
export type Status = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'ESCALATED';
export type Severity = 'LOW' | 'HIGH' | 'CRITICAL';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  department?: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: Role;
  department?: string;
}

export interface Ticket {
  id: number;
  title: string;
  description: string;
  category: Category;
  priority: Priority;
  status: Status;
  confidential: boolean;
  requestType?: string;
  osInfo?: string;
  browserInfo?: string;
  appVersion?: string;
  severity?: Severity;
  assetTag?: string;
  createdByName: string;
  createdById: number;
  assignedToName?: string;
  assignedToId?: number;
  estimatedResolutionHours?: number;
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string;
  satisfactionRating?: number;
  escalated: boolean;
  relatedTicketIds: number[];
}

export interface TimelineEntry {
  id: number;
  action: string;
  notes?: string;
  actorName: string;
  createdAt: string;
}

export interface Notification {
  id: number;
  message: string;
  ticketId?: number;
  read: boolean;
  createdAt: string;
}

export interface KnowledgeArticle {
  id: number;
  title: string;
  content: string;
  category?: Category;
  keywords?: string;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  category: Category;
  confidential?: boolean;
  requestType?: string;
  osInfo?: string;
  browserInfo?: string;
  appVersion?: string;
  severity?: Severity;
  assetTag?: string;
  relatedTicketIds?: number[];
}
