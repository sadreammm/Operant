import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface WorkflowStep {
  id: string;
  actionType: string;
  configData: string;
  stepOrder: number;
}

export interface Workflow {
  id: string;
  name: string;
  description: string;
  tenantId: string;
  status: string;
  createdAt: string;
  steps: WorkflowStep[];
}

export interface AgentResult {
  id: string;
  workflowId: string;
  agentOutput: string;
  status: string;
  processedAt: string;
}

export interface CreateWorkflowRequest {
  name: string;
  description: string;
  tenantId: string;
  steps: { actionType: string; configData: string; stepOrder: number }[];
}

@Injectable({
  providedIn: 'root'
})
export class WorkflowService {
  private baseUrl = 'http://localhost:8080/api/workflows';

  constructor(private http: HttpClient) {}

  getWorkflowsByTenant(tenantId: string): Observable<Workflow[]> {
    return this.http.get<Workflow[]>(`${this.baseUrl}/tenant/${tenantId}`, {
      withCredentials: true
    });
  }

  createWorkflow(request: CreateWorkflowRequest): Observable<Workflow> {
    return this.http.post<Workflow>(this.baseUrl, request, {
      withCredentials: true
    });
  }

  triggerWorkflow(workflowId: string): Observable<Workflow> {
    return this.http.post<Workflow>(`${this.baseUrl}/${workflowId}/trigger`, {}, {
      withCredentials: true
    });
  }

  getAgentResults(workflowId: string): Observable<AgentResult[]> {
    return this.http.get<AgentResult[]>(`${this.baseUrl}/${workflowId}/results`, {
      withCredentials: true
    });
  }
}
