import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TicketService } from '../../services/ticket.service';
import { KnowledgeService } from '../../services/knowledge.service';
import { UserService } from '../../services/user.service';
import { Category, Severity, Ticket, KnowledgeArticle, CreateTicketRequest } from '../../models/models';
import { Subject, debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';

@Component({
  selector: 'app-create-ticket',
  templateUrl: './create-ticket.component.html',
  styleUrls: ['./create-ticket.component.css']
})
export class CreateTicketComponent implements OnInit {
  // Step 1 = compose, Step 2 = review similar tickets, Step 3 = submitted
  step = 1;

  title = '';
  description = '';
  category: Category = 'IT';
  confidential = false;
  requestType = '';
  osInfo = '';
  browserInfo = '';
  appVersion = '';
  severity: Severity | '' = '';
  assetTag = '';

  selectedFile?: File;

  commonIssues: { [k: string]: string[] } = {};
  filteredSuggestions: string[] = [];
  showAutofill = false;

  kbArticles: KnowledgeArticle[] = [];
  similarTickets: Ticket[] = [];

  error = '';
  submitting = false;

  hrRequestTypes = ['Leave request', 'Payroll query', 'Policy clarification', 'Benefits', 'Reimbursement', 'Other'];

  private titleChange$ = new Subject<string>();

  constructor(
    private ticketService: TicketService,
    private knowledgeService: KnowledgeService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit() {
    this.userService.commonIssues().subscribe(issues => this.commonIssues = issues);

    // Debounced search for similar tickets and KB articles on title change
    this.titleChange$.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(title => {
        if (!title || title.length < 3) return of({ similar: [], kb: [] });
        return this.ticketService.similar(title, this.category).pipe(
          switchMap(similar =>
            this.knowledgeService.suggest(title, this.category).pipe(
              switchMap(kb => of({ similar, kb }))
            )
          )
        );
      })
    ).subscribe(({ similar, kb }) => {
      this.similarTickets = similar;
      this.kbArticles = kb;
    });

    this.detectEnvironment();
  }

  private detectEnvironment() {
    // Auto-detect browser/OS for bug reports
    const ua = navigator.userAgent;
    if (ua.includes('Windows')) this.osInfo = 'Windows';
    else if (ua.includes('Mac')) this.osInfo = 'macOS';
    else if (ua.includes('Linux')) this.osInfo = 'Linux';
    else if (ua.includes('Android')) this.osInfo = 'Android';
    else if (ua.includes('iPhone') || ua.includes('iPad')) this.osInfo = 'iOS';

    if (ua.includes('Chrome')) this.browserInfo = 'Chrome';
    else if (ua.includes('Firefox')) this.browserInfo = 'Firefox';
    else if (ua.includes('Safari')) this.browserInfo = 'Safari';
    else if (ua.includes('Edge')) this.browserInfo = 'Edge';
  }

  onTitleChange() {
    const list = this.commonIssues[this.category] || [];
    this.filteredSuggestions = this.title
      ? list.filter(s => s.toLowerCase().includes(this.title.toLowerCase()))
      : list;
    this.showAutofill = this.filteredSuggestions.length > 0 && this.title.length > 0;
    this.titleChange$.next(this.title);
  }

  selectSuggestion(s: string) {
    this.title = s;
    this.showAutofill = false;
    this.titleChange$.next(s);
  }

  onCategoryChange() {
    this.titleChange$.next(this.title);
    if (this.category !== 'BUG') this.severity = '';
    if (this.category !== 'IT') this.assetTag = '';
    if (this.category !== 'HR') {
      this.requestType = '';
      this.confidential = false;
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files?.[0];
    if (file) this.selectedFile = file;
  }

  proceedToReview() {
    if (this.similarTickets.length > 0) {
      this.step = 2;
    } else {
      this.submit();
    }
  }

  submit() {
    this.error = '';
    this.submitting = true;

    const payload: CreateTicketRequest = {
      title: this.title,
      description: this.description,
      category: this.category,
      confidential: this.confidential,
      requestType: this.requestType || undefined,
      osInfo: this.osInfo || undefined,
      browserInfo: this.browserInfo || undefined,
      appVersion: this.appVersion || undefined,
      severity: this.severity || undefined,
      assetTag: this.assetTag || undefined
    };

    this.ticketService.create(payload).subscribe({
      next: ticket => {
        if (this.selectedFile) {
          this.ticketService.uploadAttachment(ticket.id, this.selectedFile).subscribe({
            next: () => this.router.navigate(['/ticket', ticket.id]),
            error: () => this.router.navigate(['/ticket', ticket.id])
          });
        } else {
          this.router.navigate(['/ticket', ticket.id]);
        }
      },
      error: err => {
        this.submitting = false;
        this.error = err.error?.error || 'Failed to create ticket';
      }
    });
  }

  openSimilar(t: Ticket) {
    this.router.navigate(['/ticket', t.id]);
  }
}
