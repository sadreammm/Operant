import { Component, OnInit, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { AuthService, Tenant } from '../../services/auth';
import {
  AgentResult,
  CreateWorkflowRequest,
  Workflow,
  WorkflowService,
} from '../../services/workflow';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardComponent implements OnInit {
  tenant: Tenant | null = null;
  workflows: Workflow[] = [];
  agentResults: Map<string, AgentResult[]> = new Map();
  showCreateModal = false;
  triggeringId: string | null = null;

  newWorkflow: CreateWorkflowRequest = {
    name: '',
    description: '',
    tenantId: '',
    steps: []
  };

  constructor(
    private authService: AuthService,
    private workflowService: WorkflowService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)){
      this.authService.getCurrentTenant().subscribe({
        next: (tenant) => {
          this.tenant = tenant;
          this.newWorkflow.tenantId = tenant.id;
          this.loadWorkflows(tenant.id);
        },
        error: () => {
          this.router.navigate(['/']);
        },
      });
    }
  }

  loadWorkflows(tenantId: string): void {
    this.workflowService.getWorkflowsByTenant(tenantId).subscribe(workflows => {
      this.workflows = workflows;
      workflows.forEach(w => this.loadAgentResults(w.id));

      this.cdr.detectChanges();
    });
  }

  loadAgentResults(workflowId: string): void {
    this.workflowService.getAgentResults(workflowId).subscribe(results => {
      this.agentResults.set(workflowId, results);

      this.cdr.detectChanges();
    });
  }

  triggerWorkflow(workflowId: string): void {
    this.triggeringId = workflowId;
    this.workflowService.triggerWorkflow(workflowId).subscribe({
      next: (updatedWorkflow) => {
        this.workflows = this.workflows.map(w => w.id === workflowId ? updatedWorkflow : w);
        this.triggeringId = null;
        this.pollForResults(workflowId, 5);
      },
      error: () =>{ this.triggeringId = null; }
    });
  }

  pollForResults(workflowId: string, attemptsLeft: number): void {
    if (attemptsLeft <= 0) return;

    setTimeout(() => {
      this.workflowService.getAgentResults(workflowId).subscribe(results => {
        const currentResults = this.agentResults.get(workflowId) || [];

        if (results.length > currentResults.length) {
          this.agentResults.set(workflowId, results);

          this.cdr.detectChanges();
        } else {
          console.log(`Agent stil thinking... (${attemptsLeft - 1} attempts left)`);
          this.pollForResults(workflowId, attemptsLeft - 1);
        }
      });
    }, 3000);
  }

  addStep(): void {
    this.newWorkflow.steps.push({
      actionType: 'AI_AGENT',
      configData: '',
      stepOrder: this.newWorkflow.steps.length + 1
    });
  }

  removeStep(index: number): void {
    this.newWorkflow.steps.splice(index, 1);
    this.newWorkflow.steps.forEach((s, i) => s.stepOrder = i + 1);
  }

  createWorkflow(): void {
    if (!this.newWorkflow.name || !this.newWorkflow.description) return;
    this.workflowService.createWorkflow(this.newWorkflow).subscribe(workflow => {
      this.workflows.push(workflow);
      this.showCreateModal = false;
      this.newWorkflow = { name: '', description: '', tenantId: '', steps: []}

      this.cdr.detectChanges();
    });
  }

  getResults(workflowId: string): AgentResult[] {
    return this.agentResults.get(workflowId) || [];
  }

  getStatusClass(status: string): string {
    return status === "RUNNING" ? "badge-running" :
          status === "ACTIVE" ? "badge-active" : "badge-inactive";
  }
}
